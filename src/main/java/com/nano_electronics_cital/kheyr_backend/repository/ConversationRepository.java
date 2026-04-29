package com.nano_electronics_cital.kheyr_backend.repository;

import com.nano_electronics_cital.kheyr_backend.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String> {

    Optional<Conversation> findByDirectConversationKey(String directConversationKey);

    List<Conversation> findByParticipantIdsContainsOrderByUpdatedAtDesc(String userId);
}
