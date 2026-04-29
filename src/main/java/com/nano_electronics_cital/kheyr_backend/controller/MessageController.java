package com.nano_electronics_cital.kheyr_backend.controller;

import com.nano_electronics_cital.kheyr_backend.dto.message.DeleteMessageRequest;
import com.nano_electronics_cital.kheyr_backend.dto.message.MessageResponse;
import com.nano_electronics_cital.kheyr_backend.dto.message.SendMessageRequest;
import com.nano_electronics_cital.kheyr_backend.dto.message.UpdateMessageStatusRequest;
import com.nano_electronics_cital.kheyr_backend.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse sendMessage(@Valid @RequestBody SendMessageRequest request) {
        return messageService.sendMessage(request);
    }

    @GetMapping("/conversation/{conversationId}")
    public List<MessageResponse> getConversationMessages(
            @PathVariable String conversationId,
            @RequestParam String requesterUserId
    ) {
        return messageService.getConversationMessages(conversationId, requesterUserId);
    }

    @PatchMapping("/{messageId}/status")
    public MessageResponse updateMessageStatus(
            @PathVariable String messageId,
            @Valid @RequestBody UpdateMessageStatusRequest request
    ) {
        return messageService.updateMessageStatus(messageId, request);
    }

    @PatchMapping("/{messageId}/delete")
    public MessageResponse deleteMessage(
            @PathVariable String messageId,
            @Valid @RequestBody DeleteMessageRequest request
    ) {
        return messageService.deleteMessage(messageId, request);
    }
}
