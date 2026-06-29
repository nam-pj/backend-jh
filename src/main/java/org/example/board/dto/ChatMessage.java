package org.example.board.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    private String roomId;
    private String sender;
    private String message;
    private String receiver;

    private MessageType type;   // 추가: TALK, GAME_INVITE, GAME_ACCEPT, GAME_DECLINE
    private Long inviteId;      // 추가: 초대 식별자 (수락/거절 응답 시 사용)
}