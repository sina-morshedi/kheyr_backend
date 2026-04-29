package com.nano_electronics_cital.kheyr_backend.dto.realtime;

import com.nano_electronics_cital.kheyr_backend.dto.message.MessageResponse;
import com.nano_electronics_cital.kheyr_backend.model.enums.RealtimeEventType;

public record MessageRealtimeEvent(
        RealtimeEventType type,
        MessageResponse message
) {
}
