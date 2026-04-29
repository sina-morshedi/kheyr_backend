package com.nano_electronics_cital.kheyr_backend.service;

import com.nano_electronics_cital.kheyr_backend.dto.conversation.ConversationResponse;
import com.nano_electronics_cital.kheyr_backend.dto.conversation.CreateConversationRequest;
import com.nano_electronics_cital.kheyr_backend.dto.conversation.MarkConversationReadRequest;
import com.nano_electronics_cital.kheyr_backend.exception.BusinessException;
import com.nano_electronics_cital.kheyr_backend.exception.ResourceNotFoundException;
import com.nano_electronics_cital.kheyr_backend.mapper.ChatMapper;
import com.nano_electronics_cital.kheyr_backend.model.Conversation;
import com.nano_electronics_cital.kheyr_backend.model.Message;
import com.nano_electronics_cital.kheyr_backend.model.User;
import com.nano_electronics_cital.kheyr_backend.model.embedded.ConversationParticipant;
import com.nano_electronics_cital.kheyr_backend.model.enums.ConversationType;
import com.nano_electronics_cital.kheyr_backend.model.enums.MessageStatus;
import com.nano_electronics_cital.kheyr_backend.model.enums.ParticipantRole;
import com.nano_electronics_cital.kheyr_backend.model.enums.RealtimeEventType;
import com.nano_electronics_cital.kheyr_backend.repository.ConversationRepository;
import com.nano_electronics_cital.kheyr_backend.repository.MessageRepository;
import com.nano_electronics_cital.kheyr_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ChatMapper chatMapper;
    private final ChatRealtimeService chatRealtimeService;

    public ConversationService(
            ConversationRepository conversationRepository,
            UserRepository userRepository,
            MessageRepository messageRepository,
            ChatMapper chatMapper,
            ChatRealtimeService chatRealtimeService
    ) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.chatMapper = chatMapper;
        this.chatRealtimeService = chatRealtimeService;
    }

    public ConversationResponse createConversation(CreateConversationRequest request) {
        Set<String> participantIds = normalizeParticipantIds(request.participantUserIds(), request.createdByUserId());
        validateConversationRequest(request, participantIds);

        List<User> users = userRepository.findByIdIn(participantIds);
        if (users.size() != participantIds.size()) {
            throw new BusinessException("One or more participants do not exist.");
        }

        if (request.type() == ConversationType.DIRECT) {
            String directConversationKey = buildDirectConversationKey(participantIds);
            Conversation existingConversation = conversationRepository.findByDirectConversationKey(directConversationKey)
                    .orElse(null);
            if (existingConversation != null) {
                return chatMapper.toConversationResponse(existingConversation);
            }
        }

        Map<String, User> usersById = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        Instant now = Instant.now();
        Conversation conversation = new Conversation();
        conversation.setType(request.type());
        conversation.setParticipantIds(new ArrayList<>(participantIds));
        conversation.setParticipants(buildParticipants(participantIds, usersById, request.createdByUserId(), now));
        conversation.setCreatedByUserId(request.createdByUserId().trim());
        conversation.setTitle(request.type() == ConversationType.GROUP ? trimToNull(request.title()) : null);
        conversation.setDescription(trimToNull(request.description()));
        conversation.setDirectConversationKey(request.type() == ConversationType.DIRECT ? buildDirectConversationKey(participantIds) : null);
        conversation.setCreatedAt(now);
        conversation.setUpdatedAt(now);

        Conversation savedConversation = conversationRepository.save(conversation);
        chatRealtimeService.publishConversationEvent(RealtimeEventType.CONVERSATION_CREATED, savedConversation);
        return chatMapper.toConversationResponse(savedConversation);
    }

    public List<ConversationResponse> getUserConversations(String userId) {
        String normalizedUserId = userId.trim();
        ensureUserExists(normalizedUserId);
        return conversationRepository.findByParticipantIdsContainsOrderByUpdatedAtDesc(normalizedUserId)
                .stream()
                .map(chatMapper::toConversationResponse)
                .toList();
    }

    public ConversationResponse getConversation(String conversationId, String requesterUserId) {
        Conversation conversation = getConversationEntity(conversationId);
        verifyParticipant(conversation, requesterUserId);
        return chatMapper.toConversationResponse(conversation);
    }

    public ConversationResponse markConversationAsRead(String conversationId, MarkConversationReadRequest request) {
        Conversation conversation = getConversationEntity(conversationId);
        String userId = request.userId().trim();
        verifyParticipant(conversation, userId);

        Message targetMessage = resolveTargetMessage(conversation, request.lastReadMessageId());
        Instant readAt = targetMessage != null ? targetMessage.getSentAt() : Instant.now();

        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
        boolean changedAnyMessage = false;

        for (Message message : messages) {
            if (message.getSenderUserId().equals(userId)) {
                continue;
            }

            if (targetMessage != null && message.getSentAt().isAfter(targetMessage.getSentAt())) {
                continue;
            }

            boolean changedCurrentMessage = false;
            if (message.getDeliveredToUserIds().add(userId)) {
                changedCurrentMessage = true;
            }
            if (message.getSeenByUserIds().add(userId)) {
                message.setStatus(MessageStatus.SEEN);
                changedCurrentMessage = true;
            }

            if (changedCurrentMessage) {
                changedAnyMessage = true;
            }
        }

        if (changedAnyMessage) {
            messageRepository.saveAll(messages);
        }

        updateParticipantReadState(conversation, userId, targetMessage != null ? targetMessage.getId() : null, readAt, 0);
        Conversation savedConversation = conversationRepository.save(conversation);

        if (targetMessage != null) {
            chatRealtimeService.publishMessageEvent(RealtimeEventType.MESSAGE_STATUS_UPDATED, savedConversation, targetMessage);
        } else {
            chatRealtimeService.publishConversationEvent(RealtimeEventType.CONVERSATION_UPDATED, savedConversation);
        }

        return chatMapper.toConversationResponse(savedConversation);
    }

    public Conversation getConversationEntity(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));
    }

    private void ensureUserExists(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    private void validateConversationRequest(CreateConversationRequest request, Set<String> participantIds) {
        if (!participantIds.contains(request.createdByUserId().trim())) {
            throw new BusinessException("Conversation creator must be included in participants.");
        }

        if (request.type() == ConversationType.DIRECT && participantIds.size() != 2) {
            throw new BusinessException("Direct conversations must contain exactly 2 unique participants.");
        }

        if (request.type() == ConversationType.GROUP && participantIds.size() < 2) {
            throw new BusinessException("Group conversations must contain at least 2 participants.");
        }
    }

    private List<ConversationParticipant> buildParticipants(
            Collection<String> participantIds,
            Map<String, User> usersById,
            String createdByUserId,
            Instant now
    ) {
        return participantIds.stream()
                .map(userId -> {
                    User user = usersById.get(userId);
                    ConversationParticipant participant = new ConversationParticipant();
                    participant.setUserId(user.getId());
                    participant.setFirstName(user.getFirstName());
                    participant.setLastName(user.getLastName());
                    participant.setDisplayName(user.getDisplayName());
                    participant.setRole(user.getId().equals(createdByUserId.trim()) ? ParticipantRole.OWNER : ParticipantRole.MEMBER);
                    participant.setJoinedAt(now);
                    participant.setLastReadAt(now);
                    participant.setUnreadMessageCount(0);
                    return participant;
                })
                .toList();
    }

    private Set<String> normalizeParticipantIds(List<String> participantIds, String createdByUserId) {
        LinkedHashSet<String> normalizedParticipantIds = participantIds.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        normalizedParticipantIds.add(createdByUserId.trim());
        return normalizedParticipantIds;
    }

    private String buildDirectConversationKey(Collection<String> participantIds) {
        return participantIds.stream()
                .sorted()
                .collect(Collectors.joining(":"));
    }

    private Message resolveTargetMessage(Conversation conversation, String lastReadMessageId) {
        if (StringUtils.hasText(lastReadMessageId)) {
            Message message = messageRepository.findById(lastReadMessageId.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Last read message not found."));
            if (!conversation.getId().equals(message.getConversationId())) {
                throw new BusinessException("Last read message must belong to the same conversation.");
            }
            return message;
        }

        return messageRepository.findTopByConversationIdOrderBySentAtDesc(conversation.getId()).orElse(null);
    }

    private void updateParticipantReadState(
            Conversation conversation,
            String userId,
            String lastReadMessageId,
            Instant lastReadAt,
            int unreadMessageCount
    ) {
        for (ConversationParticipant participant : conversation.getParticipants()) {
            if (participant.getUserId().equals(userId)) {
                participant.setLastReadMessageId(lastReadMessageId);
                participant.setLastReadAt(lastReadAt);
                participant.setUnreadMessageCount(unreadMessageCount);
                return;
            }
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private void verifyParticipant(Conversation conversation, String requesterUserId) {
        if (!conversation.getParticipantIds().contains(requesterUserId.trim())) {
            throw new BusinessException("User is not a participant of this conversation.");
        }
    }
}
