package org.example.board.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class RoomMessage {
    private String roomId;
    private String sender;
    private MessageType type;
    private String message;

    private Map<String, RoomPlayer> players;
    private String winner;

    // 끝말잇기 전용
    private String currentTurn;   // 현재 차례 유저명
    private String lastWord;       // 마지막 단어
    private List<String> usedWords; // 사용된 단어 목록
    private String reason;         // 탈락 이유
}