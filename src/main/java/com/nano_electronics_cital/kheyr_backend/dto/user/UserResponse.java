package com.nano_electronics_cital.kheyr_backend.dto.user;

import java.time.Instant;

public record UserResponse(
        String id,
        String firstName,
        String lastName,
        String displayName,
        String phoneNumber,
        String emailAddress,
        String countryIsoCode,
        String avatarUrl,
        String profileStatus,
        boolean phoneVerified,
        boolean emailVerified,
        Instant lastSeenAt,
        Instant createdAt,
        Instant updatedAt
) {
}
