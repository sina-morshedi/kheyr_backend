package com.nano_electronics_cital.kheyr_backend.mapper;

import com.nano_electronics_cital.kheyr_backend.dto.conversation.ConversationParticipantResponse;
import com.nano_electronics_cital.kheyr_backend.dto.conversation.ConversationResponse;
import com.nano_electronics_cital.kheyr_backend.dto.message.MessageAttachmentResponse;
import com.nano_electronics_cital.kheyr_backend.dto.message.MessageResponse;
import com.nano_electronics_cital.kheyr_backend.model.Conversation;
import com.nano_electronics_cital.kheyr_backend.model.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChatMapper {

    private static final String DELETED_MESSAGE_PLACEHOLDER = "This message was deleted.";

    public ConversationResponse toConversationResponse(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getCreatedByUserId(),
                conversation.getTitle(),
                conversation.getDescription(),
                conversation.getParticipants()
                        .stream()
                        .map(participant -> new ConversationParticipantResponse(
                                participant.getUserId(),
                                participant.getFirstName(),
                                participant.getLastName(),
                                participant.getDisplayName(),
                                participant.getRole(),
                                participant.getJoinedAt(),
                                participant.getLastReadMessageId(),
                                participant.getLastReadAt(),
                                participant.getUnreadMessageCount()
                        ))
                        .toList(),
                conversation.getLastMessageId(),
                conversation.getLastMessagePreview(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }

    public MessageResponse toMessageResponse(Message message) {
        boolean deletedForEveryone = message.isDeletedForEveryone();

        return new MessageResponse(
                message.getId(),
                message.getConversationId(),
                message.getSenderUserId(),
                message.getType(),
                deletedForEveryone ? DELETED_MESSAGE_PLACEHOLDER : message.getContent(),
                message.getReplyToMessageId(),
                deletedForEveryone
                        ? List.of()
                        : message.getAttachments()
                        .stream()
                        .map(attachment -> new MessageAttachmentResponse(
                                attachment.getFileName(),
                                attachment.getContentType(),
                                attachment.getResourceUrl(),
                                attachment.getFileSizeBytes()
                        ))
                        .toList(),
                message.getStatus(),
                message.getDeliveredToUserIds(),
                message.getSeenByUserIds(),
                deletedForEveryone,
                message.getDeletedByUserId(),
                message.getDeletedAt(),
                message.getSentAt(),
                message.getEditedAt()
        );
    }
}
