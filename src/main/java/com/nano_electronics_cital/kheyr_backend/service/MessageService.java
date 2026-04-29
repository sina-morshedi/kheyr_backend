package com.nano_electronics_cital.kheyr_backend.service;

import com.nano_electronics_cital.kheyr_backend.dto.message.DeleteMessageRequest;
import com.nano_electronics_cital.kheyr_backend.dto.message.MessageAttachmentRequest;
import com.nano_electronics_cital.kheyr_backend.dto.message.MessageResponse;
import com.nano_electronics_cital.kheyr_backend.dto.message.SendMessageRequest;
import com.nano_electronics_cital.kheyr_backend.dto.message.UpdateMessageStatusRequest;
import com.nano_electronics_cital.kheyr_backend.exception.BusinessException;
import com.nano_electronics_cital.kheyr_backend.exception.ResourceNotFoundException;
import com.nano_electronics_cital.kheyr_backend.mapper.ChatMapper;
import com.nano_electronics_cital.kheyr_backend.model.Conversation;
import com.nano_electronics_cital.kheyr_backend.model.Message;
import com.nano_electronics_cital.kheyr_backend.model.embedded.ConversationParticipant;
import com.nano_electronics_cital.kheyr_backend.model.embedded.MessageAttachment;
import com.nano_electronics_cital.kheyr_backend.model.enums.MessageStatus;
import com.nano_electronics_cital.kheyr_backend.model.enums.MessageType;
import com.nano_electronics_cital.kheyr_backend.model.enums.ParticipantRole;
import com.nano_electronics_cital.kheyr_backend.model.enums.RealtimeEventType;
import com.nano_electronics_cital.kheyr_backend.repository.ConversationRepository;
import com.nano_electronics_cital.kheyr_backend.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMapper chatMapper;
    private final ChatRealtimeService chatRealtimeService;

    public MessageService(
            MessageRepository messageRepository,
            ConversationRepository conversationRepository,
            ChatMapper chatMapper,
            ChatRealtimeService chatRealtimeService
    ) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.chatMapper = chatMapper;
        this.chatRealtimeService = chatRealtimeService;
    }

    public MessageResponse sendMessage(SendMessageRequest request) {
        Conversation conversation = getConversation(request.conversationId());
        verifyParticipant(conversation, request.senderUserId());
        validateMessageRequest(request);

        if (StringUtils.hasText(request.replyToMessageId())) {
            Message repliedMessage = messageRepository.findById(request.replyToMessageId())
                    .orElseThrow(() -> new ResourceNotFoundException("Reply target message not found."));
            if (!conversation.getId().equals(repliedMessage.getConversationId())) {
                throw new BusinessException("Reply target must belong to the same conversation.");
            }
        }

        Instant now = Instant.now();
        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderUserId(request.senderUserId().trim());
        message.setType(request.type());
        message.setContent(trimToNull(request.content()));
        message.setReplyToMessageId(trimToNull(request.replyToMessageId()));
        message.setAttachments(toAttachments(request.attachments()));
        message.setStatus(MessageStatus.SENT);
        message.setDeliveredToUserIds(new HashSet<>(Set.of(request.senderUserId().trim())));
        message.setSeenByUserIds(new HashSet<>(Set.of(request.senderUserId().trim())));
        message.setSentAt(now);

        Message savedMessage = messageRepository.save(message);

        conversation.setLastMessageId(savedMessage.getId());
        conversation.setLastMessagePreview(buildPreview(savedMessage));
        conversation.setUpdatedAt(now);
        updateUnreadStateAfterSend(conversation, savedMessage, now);

        Conversation savedConversation = conversationRepository.save(conversation);
        chatRealtimeService.publishMessageEvent(RealtimeEventType.MESSAGE_CREATED, savedConversation, savedMessage);

        return chatMapper.toMessageResponse(savedMessage);
    }

    public List<MessageResponse> getConversationMessages(String conversationId, String requesterUserId) {
        Conversation conversation = getConversation(conversationId);
        verifyParticipant(conversation, requesterUserId);
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId)
                .stream()
                .map(chatMapper::toMessageResponse)
                .toList();
    }

    public MessageResponse updateMessageStatus(String messageId, UpdateMessageStatusRequest request) {
        if (request.status() == MessageStatus.SENT) {
            throw new BusinessException("Message status can only be updated to DELIVERED or SEEN.");
        }

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));
        Conversation conversation = getConversation(message.getConversationId());
        String userId = request.userId().trim();
        verifyParticipant(conversation, userId);

        if (message.getSenderUserId().equals(userId)) {
            throw new BusinessException("Sender cannot update status for their own message.");
        }

        if (request.status() == MessageStatus.DELIVERED) {
            message.getDeliveredToUserIds().add(userId);
            if (message.getStatus() == MessageStatus.SENT) {
                message.setStatus(MessageStatus.DELIVERED);
            }
        }

        if (request.status() == MessageStatus.SEEN) {
            boolean messageWasUnread = !message.getSeenByUserIds().contains(userId);
            message.getDeliveredToUserIds().add(userId);
            message.getSeenByUserIds().add(userId);
            message.setStatus(MessageStatus.SEEN);
            markConversationRead(conversation, userId, message.getId(), message.getSentAt());
            if (messageWasUnread) {
                decreaseUnreadCount(conversation, userId);
            }
        }

        Message savedMessage = messageRepository.save(message);
        Conversation savedConversation = conversationRepository.save(conversation);
        chatRealtimeService.publishMessageEvent(RealtimeEventType.MESSAGE_STATUS_UPDATED, savedConversation, savedMessage);
        return chatMapper.toMessageResponse(savedMessage);
    }

    public MessageResponse deleteMessage(String messageId, DeleteMessageRequest request) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with id: " + messageId));
        Conversation conversation = getConversation(message.getConversationId());
        String userId = request.userId().trim();
        verifyParticipant(conversation, userId);

        if (!canDeleteMessage(conversation, message, userId)) {
            throw new BusinessException("User is not allowed to delete this message.");
        }

        if (!message.isDeletedForEveryone()) {
            message.setDeletedForEveryone(true);
            message.setDeletedByUserId(userId);
            message.setDeletedAt(Instant.now());
            message.setContent(null);
            message.setAttachments(List.of());

            if (message.getId().equals(conversation.getLastMessageId())) {
                conversation.setLastMessagePreview(buildPreview(message));
            }

            decreaseUnreadCountAfterDelete(conversation, message);
        }

        Message savedMessage = messageRepository.save(message);
        Conversation savedConversation = conversationRepository.save(conversation);
        chatRealtimeService.publishMessageEvent(RealtimeEventType.MESSAGE_DELETED, savedConversation, savedMessage);
        return chatMapper.toMessageResponse(savedMessage);
    }

    private void validateMessageRequest(SendMessageRequest request) {
        boolean hasTextContent = StringUtils.hasText(request.content());
        boolean hasAttachments = request.attachments() != null && !request.attachments().isEmpty();

        if (request.type() == MessageType.TEXT && !hasTextContent) {
            throw new BusinessException("Text message content cannot be empty.");
        }

        if (!hasTextContent && !hasAttachments) {
            throw new BusinessException("Message must contain text or at least one attachment.");
        }
    }

    private List<MessageAttachment> toAttachments(List<MessageAttachmentRequest> attachmentRequests) {
        if (attachmentRequests == null || attachmentRequests.isEmpty()) {
            return List.of();
        }

        return attachmentRequests.stream()
                .map(request -> {
                    MessageAttachment attachment = new MessageAttachment();
                    attachment.setFileName(request.fileName().trim());
                    attachment.setContentType(request.contentType().trim());
                    attachment.setResourceUrl(request.resourceUrl().trim());
                    attachment.setFileSizeBytes(request.fileSizeBytes());
                    return attachment;
                })
                .toList();
    }

    private String buildPreview(Message message) {
        if (message.isDeletedForEveryone()) {
            return "This message was deleted.";
        }

        if (StringUtils.hasText(message.getContent())) {
            String content = message.getContent().trim();
            return content.length() > 80 ? content.substring(0, 80) : content;
        }

        return switch (message.getType()) {
            case IMAGE -> "[Image]";
            case VIDEO -> "[Video]";
            case AUDIO -> "[Audio]";
            case FILE -> "[File]";
            case LOCATION -> "[Location]";
            case SYSTEM -> "[System]";
            default -> "[Message]";
        };
    }

    private Conversation getConversation(String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with id: " + conversationId));
    }

    private void verifyParticipant(Conversation conversation, String userId) {
        if (!conversation.getParticipantIds().contains(userId.trim())) {
            throw new BusinessException("User is not a participant of this conversation.");
        }
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private void updateUnreadStateAfterSend(Conversation conversation, Message message, Instant now) {
        for (ConversationParticipant participant : conversation.getParticipants()) {
            if (participant.getUserId().equals(message.getSenderUserId())) {
                participant.setLastReadMessageId(message.getId());
                participant.setLastReadAt(now);
                participant.setUnreadMessageCount(0);
                continue;
            }

            participant.setUnreadMessageCount(participant.getUnreadMessageCount() + 1);
        }
    }

    private void markConversationRead(Conversation conversation, String userId, String messageId, Instant readAt) {
        for (ConversationParticipant participant : conversation.getParticipants()) {
            if (participant.getUserId().equals(userId)) {
                participant.setLastReadMessageId(messageId);
                participant.setLastReadAt(readAt);
                return;
            }
        }
    }

    private void decreaseUnreadCount(Conversation conversation, String userId) {
        for (ConversationParticipant participant : conversation.getParticipants()) {
            if (participant.getUserId().equals(userId)) {
                participant.setUnreadMessageCount(Math.max(0, participant.getUnreadMessageCount() - 1));
                return;
            }
        }
    }

    private void decreaseUnreadCountAfterDelete(Conversation conversation, Message message) {
        for (ConversationParticipant participant : conversation.getParticipants()) {
            if (participant.getUserId().equals(message.getSenderUserId())) {
                continue;
            }

            if (!message.getSeenByUserIds().contains(participant.getUserId())) {
                participant.setUnreadMessageCount(Math.max(0, participant.getUnreadMessageCount() - 1));
            }
        }
    }

    private boolean canDeleteMessage(Conversation conversation, Message message, String userId) {
        if (message.getSenderUserId().equals(userId)) {
            return true;
        }

        return conversation.getParticipants()
                .stream()
                .filter(participant -> participant.getUserId().equals(userId))
                .map(ConversationParticipant::getRole)
                .anyMatch(role -> role == ParticipantRole.OWNER || role == ParticipantRole.ADMIN);
    }
}
