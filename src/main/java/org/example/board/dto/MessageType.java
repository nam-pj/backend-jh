package org.example.board.dto;

public enum MessageType {
    TALK,         // 일반 채팅
    GAME_INVITE,  // 게임 초대
    GAME_ACCEPT,  // 게임 초대 수락
    GAME_DECLINE, // 게임 초대 거절
    GAME_MOVE,   // 추가: 오목 착수
    GAME_JOIN,
    ERROR,         // 예외 발생 알림 (요청한 사람에게만 전달)

    // 룸 관련 추가
    ROOM_JOIN,
    ROOM_LEAVE,
    ROOM_START,
    ROOM_STATE,
    ROOM_DEAD,
    ROOM_END,
    ROOM_WORD,      // 단어 입력
    ROOM_WRONG,     // 틀린 단어
    ROOM_TIMEOUT,   // 시간 초과 탈락
    ROOM_TIMER,
    PLAYER_INPUT
}