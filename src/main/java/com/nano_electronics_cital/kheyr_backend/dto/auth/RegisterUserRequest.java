package com.nano_electronics_cital.kheyr_backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotBlank @Pattern(regexp = "^\\+?[1-9]\\d{7,14}$", message = "Phone number must be in international format.") String phoneNumber,
        @Email @Size(max = 150) String emailAddress,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{2}$", message = "Country ISO code must contain 2 letters.") String countryIsoCode,
        @Size(max = 120) String avatarUrl,
        @Size(max = 160) String profileStatus,
        @Size(max = 100) String deviceId,
        @Size(max = 100) String deviceName,
        @Size(max = 40) String platform
) {
}
