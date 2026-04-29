package com.nano_electronics_cital.kheyr_backend.dto.conversation;

import com.nano_electronics_cital.kheyr_backend.model.enums.ParticipantRole;

import java.time.Instant;

public record ConversationParticipantResponse(
        String userId,
        String firstName,
        String lastName,
        String displayName,
        ParticipantRole role,
        Instant joinedAt,
        String lastReadMessageId,
        Instant lastReadAt,
        int unreadMessageCount
) {
}
