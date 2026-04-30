package com.nano_electronics_cital.kheyr_backend.repository;

import com.nano_electronics_cital.kheyr_backend.model.DeviceToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends MongoRepository<DeviceToken, String> {

    Optional<DeviceToken> findByDeviceToken(String deviceToken);

    List<DeviceToken> findAllByUserIdAndActiveTrue(String userId);
}
