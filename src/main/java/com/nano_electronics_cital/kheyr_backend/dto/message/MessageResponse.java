package com.nano_electronics_cital.kheyr_backend.dto.message;

import com.nano_electronics_cital.kheyr_backend.model.enums.MessageStatus;
import com.nano_electronics_cital.kheyr_backend.model.enums.MessageType;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record MessageResponse(
        String id,
        String conversationId,
        String senderUserId,
        MessageType type,
        String content,
        String replyToMessageId,
        List<MessageAttachmentResponse> attachments,
        MessageStatus status,
        Set<String> deliveredToUserIds,
        Set<String> seenByUserIds,
        boolean deletedForEveryone,
        String deletedByUserId,
        Instant deletedAt,
        Instant sentAt,
        Instant editedAt
) {
}
