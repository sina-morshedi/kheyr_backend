package com.nano_electronics_cital.kheyr_backend.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageAttachmentRequest(
        @NotBlank @Size(max = 160) String fileName,
        @NotBlank @Size(max = 100) String contentType,
        @NotBlank @Size(max = 500) String resourceUrl,
        Long fileSizeBytes
) {
}
