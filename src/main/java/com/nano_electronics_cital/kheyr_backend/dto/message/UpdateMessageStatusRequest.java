package com.nano_electronics_cital.kheyr_backend.dto.message;

import com.nano_electronics_cital.kheyr_backend.model.enums.MessageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateMessageStatusRequest(
        @NotBlank String userId,
        @NotNull MessageStatus status
) {
}
