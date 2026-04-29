package com.nano_electronics_cital.kheyr_backend.service;

import com.nano_electronics_cital.kheyr_backend.dto.auth.AuthResponse;
import com.nano_electronics_cital.kheyr_backend.dto.auth.LoginRequest;
import com.nano_electronics_cital.kheyr_backend.dto.auth.RegisterUserRequest;
import com.nano_electronics_cital.kheyr_backend.dto.user.UserResponse;
import com.nano_electronics_cital.kheyr_backend.exception.BusinessException;
import com.nano_electronics_cital.kheyr_backend.exception.ResourceNotFoundException;
import com.nano_electronics_cital.kheyr_backend.model.AuthSession;
import com.nano_electronics_cital.kheyr_backend.model.User;
import com.nano_electronics_cital.kheyr_backend.repository.AuthSessionRepository;
import com.nano_electronics_cital.kheyr_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private static final Duration DEFAULT_SESSION_TTL = Duration.ofDays(30);

    private final UserRepository userRepository;
    private final AuthSessionRepository authSessionRepository;

    public AuthService(
            UserRepository userRepository,
            AuthSessionRepository authSessionRepository
    ) {
        this.userRepository = userRepository;
        this.authSessionRepository = authSessionRepository;
    }

    public AuthResponse register(RegisterUserRequest request) {
        String normalizedPhoneNumber = normalizePhoneNumber(request.phoneNumber());
        String normalizedEmail = normalizeEmail(request.emailAddress());
        String countryIsoCode = normalizeCountryIsoCode(request.countryIsoCode());

        if (userRepository.existsByPhoneNumber(normalizedPhoneNumber)) {
            throw new BusinessException("Phone number is already registered.");
        }

        if (StringUtils.hasText(normalizedEmail) && userRepository.existsByEmailAddress(normalizedEmail)) {
            throw new BusinessException("Email address is already registered.");
        }

        Instant now = Instant.now();
        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setDisplayName(buildDisplayName(request.firstName(), request.lastName()));
        user.setPhoneNumber(normalizedPhoneNumber);
        user.setEmailAddress(normalizedEmail);
        user.setCountryIsoCode(countryIsoCode);
        user.setAvatarUrl(trimToNull(request.avatarUrl()));
        user.setProfileStatus(trimToNull(request.profileStatus()));
        user.setPhoneVerified(false);
        user.setEmailVerified(false);
        user.setLastSeenAt(now);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);
        AuthSession session = createSession(savedUser.getId(), request.deviceId(), request.deviceName(), request.platform(), now);
        return toAuthResponse(savedUser, session);
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedPhoneNumber = normalizePhoneNumber(request.phoneNumber());
        String normalizedEmail = normalizeEmail(request.emailAddress());

        if (!StringUtils.hasText(normalizedPhoneNumber) && !StringUtils.hasText(normalizedEmail)) {
            throw new BusinessException("Phone number or email address is required for login.");
        }

        User user = StringUtils.hasText(normalizedPhoneNumber)
                ? userRepository.findByPhoneNumber(normalizedPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone number: " + normalizedPhoneNumber))
                : userRepository.findByEmailAddress(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email address: " + normalizedEmail));

        Instant now = Instant.now();
        user.setLastSeenAt(now);
        user.setUpdatedAt(now);
        User savedUser = userRepository.save(user);

        AuthSession session = createSession(savedUser.getId(), request.deviceId(), request.deviceName(), request.platform(), now);
        return toAuthResponse(savedUser, session);
    }

    public void logout(String sessionToken) {
        AuthSession session = authSessionRepository.findBySessionTokenAndActiveTrue(sessionToken)
                .orElseThrow(() -> new ResourceNotFoundException("Active session not found."));
        session.setActive(false);
        session.setRevokedAt(Instant.now());
        authSessionRepository.save(session);
    }

    private AuthSession createSession(
            String userId,
            String deviceId,
            String deviceName,
            String platform,
            Instant now
    ) {
        AuthSession session = new AuthSession();
        session.setUserId(userId);
        session.setSessionToken(UUID.randomUUID().toString());
        session.setDeviceId(trimToNull(deviceId));
        session.setDeviceName(trimToNull(deviceName));
        session.setPlatform(trimToNull(platform));
        session.setActive(true);
        session.setCreatedAt(now);
        session.setExpiresAt(now.plus(DEFAULT_SESSION_TTL));
        return authSessionRepository.save(session);
    }

    private AuthResponse toAuthResponse(User user, AuthSession session) {
        return new AuthResponse(
                session.getId(),
                session.getSessionToken(),
                session.getExpiresAt(),
                new UserResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getDisplayName(),
                        user.getPhoneNumber(),
                        user.getEmailAddress(),
                        user.getCountryIsoCode(),
                        user.getAvatarUrl(),
                        user.getProfileStatus(),
                        user.isPhoneVerified(),
                        user.isEmailVerified(),
                        user.getLastSeenAt(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()
                )
        );
    }

    private String buildDisplayName(String firstName, String lastName) {
        return (firstName.trim() + " " + lastName.trim()).trim();
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return null;
        }
        return phoneNumber.replaceAll("\\s+", "");
    }

    private String normalizeEmail(String emailAddress) {
        if (!StringUtils.hasText(emailAddress)) {
            return null;
        }
        return emailAddress.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeCountryIsoCode(String countryIsoCode) {
        return countryIsoCode.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
