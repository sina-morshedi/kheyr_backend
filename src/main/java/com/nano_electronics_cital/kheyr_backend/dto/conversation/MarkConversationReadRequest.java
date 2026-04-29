package com.nano_electronics_cital.kheyr_backend.dto.conversation;

import jakarta.validation.constraints.NotBlank;

public record MarkConversationReadRequest(
        @NotBlank String userId,
        String lastReadMessageId
) {
}
