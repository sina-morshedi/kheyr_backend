package com.nano_electronics_cital.kheyr_backend.service;

import com.nano_electronics_cital.kheyr_backend.dto.realtime.ConversationRealtimeEvent;
import com.nano_electronics_cital.kheyr_backend.dto.realtime.MessageRealtimeEvent;
import com.nano_electronics_cital.kheyr_backend.mapper.ChatMapper;
import com.nano_electronics_cital.kheyr_backend.model.Conversation;
import com.nano_electronics_cital.kheyr_backend.model.Message;
import com.nano_electronics_cital.kheyr_backend.model.enums.RealtimeEventType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatRealtimeService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMapper chatMapper;

    public ChatRealtimeService(
            SimpMessagingTemplate messagingTemplate,
            ChatMapper chatMapper
    ) {
        this.messagingTemplate = messagingTemplate;
        this.chatMapper = chatMapper;
    }

    public void publishConversationEvent(RealtimeEventType type, Conversation conversation) {
        ConversationRealtimeEvent event = new ConversationRealtimeEvent(type, chatMapper.toConversationResponse(conversation));
        for (String participantId : conversation.getParticipantIds()) {
            messagingTemplate.convertAndSend("/topic/users/" + participantId + "/conversations", event);
        }
    }

    public void publishMessageEvent(RealtimeEventType type, Conversation conversation, Message message) {
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversation.getId(),
                new MessageRealtimeEvent(type, chatMapper.toMessageResponse(message))
        );
        publishConversationEvent(RealtimeEventType.CONVERSATION_UPDATED, conversation);
    }
}
