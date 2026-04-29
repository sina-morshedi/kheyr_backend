package com.nano_electronics_cital.kheyr_backend.repository;

import com.nano_electronics_cital.kheyr_backend.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findByConversationIdOrderBySentAtAsc(String conversationId);

    Optional<Message> findTopByConversationIdOrderBySentAtDesc(String conversationId);
}
