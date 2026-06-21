package org.example.board.dto;

public record DmSendRequest(
        String receiverUsername,
        String content
) {}