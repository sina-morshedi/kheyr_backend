package com.nano_electronics_cital.kheyr_backend.dto.message;

import jakarta.validation.constraints.NotBlank;

public record DeleteMessageRequest(
        @NotBlank String userId
) {
}
