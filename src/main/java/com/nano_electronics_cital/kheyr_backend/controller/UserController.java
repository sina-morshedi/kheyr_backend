package com.nano_electronics_cital.kheyr_backend.controller;

import com.nano_electronics_cital.kheyr_backend.dto.user.UserResponse;
import com.nano_electronics_cital.kheyr_backend.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable String userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/lookup")
    public UserResponse getUserByPhoneNumber(@RequestParam String phoneNumber) {
        return userService.getUserByPhoneNumber(phoneNumber);
    }
}
