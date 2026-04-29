package com.nano_electronics_cital.kheyr_backend.controller;

import com.nano_electronics_cital.kheyr_backend.dto.conversation.MarkConversationReadRequest;
import com.nano_electronics_cital.kheyr_backend.dto.message.DeleteMessageRequest;
import com.nano_electronics_cital.kheyr_backend.dto.message.SendMessageRequest;
import com.nano_electronics_cital.kheyr_backend.dto.message.UpdateMessageStatusRequest;
import com.nano_electronics_cital.kheyr_backend.service.ConversationService;
import com.nano_electronics_cital.kheyr_backend.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class RealtimeChatController {

    private final MessageService messageService;
    private final ConversationService conversationService;

    public RealtimeChatController(
            MessageService messageService,
            ConversationService conversationService
    ) {
        this.messageService = messageService;
        this.conversationService = conversationService;
    }

    @MessageMapping("/messages.send")
    public void sendMessage(@Valid SendMessageRequest request) {
        messageService.sendMessage(request);
    }

    @MessageMapping("/messages/{messageId}/status")
    public void updateMessageStatus(
            @DestinationVariable String messageId,
            @Valid UpdateMessageStatusRequest request
    ) {
        messageService.updateMessageStatus(messageId, request);
    }

    @MessageMapping("/messages/{messageId}/delete")
    public void deleteMessage(
            @DestinationVariable String messageId,
            @Valid DeleteMessageRequest request
    ) {
        messageService.deleteMessage(messageId, request);
    }

    @MessageMapping("/conversations/{conversationId}/read")
    public void markConversationAsRead(
            @DestinationVariable String conversationId,
            @Valid MarkConversationReadRequest request
    ) {
        conversationService.markConversationAsRead(conversationId, request);
    }
}
