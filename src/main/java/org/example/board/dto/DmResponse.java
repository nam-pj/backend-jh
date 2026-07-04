package org.example.board.dto;

import org.example.board.entity.DirectMessage;
import java.time.LocalDateTime;

public record DmResponse(
        Long id,
        String senderUsername,
        String receiverUsername,
        String content,
        String type,
        String status,
        String roomId,
        LocalDateTime sentAt
) {
    public static DmResponse from(DirectMessage message) {
        return new DmResponse(
                message.getId(),
                message.getSenderUsername(),
                message.getReceiverUsername(),
                message.getContent(),
                message.getType(),
                message.getStatus().name(), // enum → String 변환
                message.getRoomId(),
                message.getSentAt()
        );
    }
}