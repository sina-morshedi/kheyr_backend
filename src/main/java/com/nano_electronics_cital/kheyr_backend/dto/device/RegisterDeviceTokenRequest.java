package com.nano_electronics_cital.kheyr_backend.dto.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDeviceTokenRequest(
        @NotBlank String userId,
        @NotBlank String deviceToken,
        @NotBlank String platform,
        @Size(max = 40) String appVersion
) {
}
