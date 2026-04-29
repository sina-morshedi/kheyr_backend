package com.nano_electronics_cital.kheyr_backend.controller;

import com.nano_electronics_cital.kheyr_backend.dto.conversation.MarkConversationReadRequest;
import com.nano_electronics_cital.kheyr_backend.dto.conversation.ConversationResponse;
import com.nano_electronics_cital.kheyr_backend.dto.conversation.CreateConversationRequest;
import com.nano_electronics_cital.kheyr_backend.service.ConversationService;
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
@RequestMapping("/api/v1/conversations")
public class ConversationController {

    private final ConversationService conversationService;

    public ConversationController(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse createConversation(@Valid @RequestBody CreateConversationRequest request) {
        return conversationService.createConversation(request);
    }

    @GetMapping
    public List<ConversationResponse> getUserConversations(@RequestParam String userId) {
        return conversationService.getUserConversations(userId);
    }

    @GetMapping("/{conversationId}")
    public ConversationResponse getConversation(
            @PathVariable String conversationId,
            @RequestParam String requesterUserId
    ) {
        return conversationService.getConversation(conversationId, requesterUserId);
    }

    @PatchMapping("/{conversationId}/read")
    public ConversationResponse markConversationAsRead(
            @PathVariable String conversationId,
            @Valid @RequestBody MarkConversationReadRequest request
    ) {
        return conversationService.markConversationAsRead(conversationId, request);
    }
}
