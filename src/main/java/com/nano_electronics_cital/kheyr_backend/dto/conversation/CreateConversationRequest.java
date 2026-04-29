package com.nano_electronics_cital.kheyr_backend.dto.conversation;

import com.nano_electronics_cital.kheyr_backend.model.enums.ConversationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateConversationRequest(
        @NotNull ConversationType type,
        @NotBlank String createdByUserId,
        @NotEmpty List<@NotBlank String> participantUserIds,
        @Size(max = 120) String title,
        @Size(max = 300) String description
) {
}
