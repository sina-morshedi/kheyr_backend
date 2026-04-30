package com.nano_electronics_cital.kheyr_backend.service;

import com.nano_electronics_cital.kheyr_backend.dto.device.RegisterDeviceTokenRequest;
import com.nano_electronics_cital.kheyr_backend.dto.device.RegisterDeviceTokenResponse;
import com.nano_electronics_cital.kheyr_backend.exception.ResourceNotFoundException;
import com.nano_electronics_cital.kheyr_backend.model.DeviceToken;
import com.nano_electronics_cital.kheyr_backend.repository.DeviceTokenRepository;
import com.nano_electronics_cital.kheyr_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Service
public class DeviceTokenService {

    private static final Logger log = LoggerFactory.getLogger(DeviceTokenService.class);

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    public DeviceTokenService(
            DeviceTokenRepository deviceTokenRepository,
            UserRepository userRepository
    ) {
        this.deviceTokenRepository = deviceTokenRepository;
        this.userRepository = userRepository;
    }

    public RegisterDeviceTokenResponse register(RegisterDeviceTokenRequest request) {
        String userId = request.userId().trim();
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        log.info(
                "KHEYR_FCM_BACKEND register token requested | userId={} | platform={} | appVersion={} | tokenSuffix={}",
                userId,
                request.platform(),
                request.appVersion(),
                maskToken(request.deviceToken())
        );

        DeviceToken deviceToken = deviceTokenRepository.findByDeviceToken(request.deviceToken().trim())
                .orElseGet(DeviceToken::new);

        Instant now = Instant.now();
        if (deviceToken.getCreatedAt() == null) {
            deviceToken.setCreatedAt(now);
        }

        deviceToken.setUserId(userId);
        deviceToken.setDeviceToken(request.deviceToken().trim());
        deviceToken.setPlatform(request.platform().trim().toLowerCase());
        deviceToken.setAppVersion(trimToNull(request.appVersion()));
        deviceToken.setActive(true);
        deviceToken.setUpdatedAt(now);
        deviceToken.setLastSeenAt(now);

        DeviceToken savedDeviceToken = deviceTokenRepository.save(deviceToken);
        log.info(
                "KHEYR_FCM_BACKEND token saved | tokenId={} | userId={} | platform={} | active={} | tokenSuffix={}",
                savedDeviceToken.getId(),
                savedDeviceToken.getUserId(),
                savedDeviceToken.getPlatform(),
                savedDeviceToken.isActive(),
                maskToken(savedDeviceToken.getDeviceToken())
        );
        return new RegisterDeviceTokenResponse(
                savedDeviceToken.getId(),
                savedDeviceToken.isActive(),
                "Device token registered successfully."
        );
    }

    public void deactivate(String deviceTokenValue) {
        if (!StringUtils.hasText(deviceTokenValue)) {
            return;
        }

        deviceTokenRepository.findByDeviceToken(deviceTokenValue.trim()).ifPresent(deviceToken -> {
            log.info(
                    "KHEYR_FCM_BACKEND deactivating token | tokenId={} | userId={} | tokenSuffix={}",
                    deviceToken.getId(),
                    deviceToken.getUserId(),
                    maskToken(deviceToken.getDeviceToken())
            );
            deviceToken.setActive(false);
            deviceToken.setUpdatedAt(Instant.now());
            deviceToken.setLastSeenAt(Instant.now());
            deviceTokenRepository.save(deviceToken);
        });
    }

    private String maskToken(String token) {
        if (!StringUtils.hasText(token)) {
            return "<empty>";
        }

        String trimmed = token.trim();
        if (trimmed.length() <= 8) {
            return trimmed;
        }

        return "..." + trimmed.substring(trimmed.length() - 8);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
