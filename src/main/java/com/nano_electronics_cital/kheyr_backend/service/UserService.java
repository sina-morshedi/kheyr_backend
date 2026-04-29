package com.nano_electronics_cital.kheyr_backend.service;

import com.nano_electronics_cital.kheyr_backend.dto.user.UserResponse;
import com.nano_electronics_cital.kheyr_backend.exception.ResourceNotFoundException;
import com.nano_electronics_cital.kheyr_backend.model.User;
import com.nano_electronics_cital.kheyr_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return toUserResponse(user);
    }

    public UserResponse getUserByPhoneNumber(String phoneNumber) {
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);
        User user = userRepository.findByPhoneNumber(normalizedPhoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with phone number: " + normalizedPhoneNumber));
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
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
        );
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return phoneNumber;
        }
        return phoneNumber.replaceAll("\\s+", "");
    }
}
