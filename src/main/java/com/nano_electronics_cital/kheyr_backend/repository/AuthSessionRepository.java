package com.nano_electronics_cital.kheyr_backend.repository;

import com.nano_electronics_cital.kheyr_backend.model.AuthSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AuthSessionRepository extends MongoRepository<AuthSession, String> {

    Optional<AuthSession> findBySessionTokenAndActiveTrue(String sessionToken);
}
