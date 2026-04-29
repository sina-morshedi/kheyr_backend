package com.nano_electronics_cital.kheyr_backend.repository;

import com.nano_electronics_cital.kheyr_backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmailAddress(String emailAddress);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmailAddress(String emailAddress);

    List<User> findByIdIn(Collection<String> ids);
}
