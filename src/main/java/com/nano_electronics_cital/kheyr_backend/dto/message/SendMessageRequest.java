package com.nano_electronics_cital.kheyr_backend.dto.message;

import com.nano_electronics_cital.kheyr_backend.model.enums.MessageType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SendMessageRequest(
        @NotBlank String conversationId,
        @NotBlank String senderUserId,
        @NotNull MessageType type,
        @Size(max = 4000) String content,
        String replyToMessageId,
        List<@Valid MessageAttachmentRequest> attachments
) {
}
