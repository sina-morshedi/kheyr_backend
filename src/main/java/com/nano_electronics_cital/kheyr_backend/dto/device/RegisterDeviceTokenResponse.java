package com.nano_electronics_cital.kheyr_backend.dto.device;

public record RegisterDeviceTokenResponse(
        String id,
        boolean active,
        String message
) {
}
