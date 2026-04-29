package com.nano_electronics_cital.kheyr_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @Size(max = 20) String phoneNumber,
        @Email @Size(max = 150) String emailAddress,
        @Size(max = 100) String deviceId,
        @Size(max = 100) String deviceName,
        @Size(max = 40) String platform
) {
}
