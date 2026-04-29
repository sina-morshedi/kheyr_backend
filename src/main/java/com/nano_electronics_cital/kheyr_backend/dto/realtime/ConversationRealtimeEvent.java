package com.nano_electronics_cital.kheyr_backend.dto.realtime;

import com.nano_electronics_cital.kheyr_backend.dto.conversation.ConversationResponse;
import com.nano_electronics_cital.kheyr_backend.model.enums.RealtimeEventType;

public record ConversationRealtimeEvent(
        RealtimeEventType type,
        ConversationResponse conversation
) {
}
