package com.nano_electronics_cital.kheyr_backend.dto.conversation;

import com.nano_electronics_cital.kheyr_backend.model.enums.ConversationType;

import java.time.Instant;
import java.util.List;

public record ConversationResponse(
        String id,
        ConversationType type,
        String createdByUserId,
        String title,
        String description,
        List<ConversationParticipantResponse> participants,
        String lastMessageId,
        String lastMessagePreview,
        Instant createdAt,
        Instant updatedAt
) {
}
