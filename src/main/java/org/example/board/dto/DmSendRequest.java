package org.example.board.dto;

public record DmSendRequest(
        String receiverUsername,
        String content,
        String type,    // "TALK", "GAME_INVITE", "GAME_ACCEPT", "GAME_DECLINE"
        String roomId   // 게임 초대 시 사용
) {}