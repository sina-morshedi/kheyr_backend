package com.nano_electronics_cital.kheyr_backend.dto.auth;

import com.nano_electronics_cital.kheyr_backend.dto.user.UserResponse;

import java.time.Instant;

public record AuthResponse(
        String sessionId,
        String sessionToken,
        Instant expiresAt,
        UserResponse user
) {
}
