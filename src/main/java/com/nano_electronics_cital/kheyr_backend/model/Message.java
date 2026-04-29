package com.nano_electronics_cital.kheyr_backend.model;

import com.nano_electronics_cital.kheyr_backend.model.embedded.MessageAttachment;
import com.nano_electronics_cital.kheyr_backend.model.enums.MessageStatus;
import com.nano_electronics_cital.kheyr_backend.model.enums.MessageType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "messages")
@CompoundIndexes({
        @CompoundIndex(name = "conversation_sent_idx", def = "{'conversationId': 1, 'sentAt': 1}"),
        @CompoundIndex(name = "sender_sent_idx", def = "{'senderUserId': 1, 'sentAt': -1}")
})
public class Message {

    @Id
    private String id;

    @Indexed
    private String conversationId;

    @Indexed
    private String senderUserId;

    private MessageType type;
    private String content;
    private String replyToMessageId;
    private List<MessageAttachment> attachments = new ArrayList<>();
    private MessageStatus status;
    private Set<String> deliveredToUserIds = new HashSet<>();
    private Set<String> seenByUserIds = new HashSet<>();
    private boolean deletedForEveryone;
    private String deletedByUserId;
    private Instant deletedAt;
    private Instant sentAt;
    private Instant editedAt;

    public Message() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(String senderUserId) {
        this.senderUserId = senderUserId;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(String replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public List<MessageAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<MessageAttachment> attachments) {
        this.attachments = attachments;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public Set<String> getDeliveredToUserIds() {
        return deliveredToUserIds;
    }

    public void setDeliveredToUserIds(Set<String> deliveredToUserIds) {
        this.deliveredToUserIds = deliveredToUserIds;
    }

    public Set<String> getSeenByUserIds() {
        return seenByUserIds;
    }

    public void setSeenByUserIds(Set<String> seenByUserIds) {
        this.seenByUserIds = seenByUserIds;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public boolean isDeletedForEveryone() {
        return deletedForEveryone;
    }

    public void setDeletedForEveryone(boolean deletedForEveryone) {
        this.deletedForEveryone = deletedForEveryone;
    }

    public String getDeletedByUserId() {
        return deletedByUserId;
    }

    public void setDeletedByUserId(String deletedByUserId) {
        this.deletedByUserId = deletedByUserId;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Instant getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(Instant editedAt) {
        this.editedAt = editedAt;
    }
}
