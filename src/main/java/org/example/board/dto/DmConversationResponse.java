package org.example.board.dto;

import java.time.LocalDateTime;

public record DmConversationResponse(
        String otherUsername,
        String lastMessage,
        LocalDateTime sentAt
) {}