package com.nano_electronics_cital.kheyr_backend.controller;

import com.nano_electronics_cital.kheyr_backend.dto.device.RegisterDeviceTokenRequest;
import com.nano_electronics_cital.kheyr_backend.dto.device.RegisterDeviceTokenResponse;
import com.nano_electronics_cital.kheyr_backend.service.DeviceTokenService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/device-tokens")
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    public DeviceTokenController(DeviceTokenService deviceTokenService) {
        this.deviceTokenService = deviceTokenService;
    }

    @PostMapping
    public RegisterDeviceTokenResponse register(@Valid @RequestBody RegisterDeviceTokenRequest request) {
        return deviceTokenService.register(request);
    }

    @DeleteMapping
    public void deactivate(@RequestParam String deviceToken) {
        deviceTokenService.deactivate(deviceToken);
    }
}
