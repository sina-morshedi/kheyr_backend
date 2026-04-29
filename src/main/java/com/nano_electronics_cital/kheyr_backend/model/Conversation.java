package com.nano_electronics_cital.kheyr_backend.model;

import com.nano_electronics_cital.kheyr_backend.model.embedded.ConversationParticipant;
import com.nano_electronics_cital.kheyr_backend.model.enums.ConversationType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "conversations")
@CompoundIndexes({
        @CompoundIndex(name = "participant_updated_idx", def = "{'participantIds': 1, 'updatedAt': -1}"),
        @CompoundIndex(name = "direct_conversation_key_idx", def = "{'directConversationKey': 1}", unique = true, sparse = true)
})
public class Conversation {

    @Id
    private String id;

    private ConversationType type;

    @Indexed
    private List<String> participantIds = new ArrayList<>();

    private List<ConversationParticipant> participants = new ArrayList<>();
    private String createdByUserId;
    private String title;
    private String description;
    private String directConversationKey;
    private String lastMessageId;
    private String lastMessagePreview;
    private Instant createdAt;
    private Instant updatedAt;

    public Conversation() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ConversationType getType() {
        return type;
    }

    public void setType(ConversationType type) {
        this.type = type;
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }

    public List<ConversationParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ConversationParticipant> participants) {
        this.participants = participants;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(String createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDirectConversationKey() {
        return directConversationKey;
    }

    public void setDirectConversationKey(String directConversationKey) {
        this.directConversationKey = directConversationKey;
    }

    public String getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(String lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
