package com.nano_electronics_cital.kheyr_backend.dto.message;

public record MessageAttachmentResponse(
        String fileName,
        String contentType,
        String resourceUrl,
        Long fileSizeBytes
) {
}
