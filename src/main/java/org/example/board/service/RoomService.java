package org.example.board.service;

import lombok.RequiredArgsConstructor;
import org.example.board.dto.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public static final int TURN_SECONDS = 30; // 제한 시간 30초

    public GameRoom createRoom(String hostUsername) {
        String roomId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        GameRoom room = new GameRoom(roomId, hostUsername);
        room.addPlayer(hostUsername);
        rooms.put(roomId, room);
        return room;
    }

    public GameRoom joinRoom(String roomId, String username) {
        GameRoom room = getRoom(roomId);
        if (room.isFull()) throw new IllegalStateException("방이 꽉 찼습니다.");
        if (room.isStarted()) throw new IllegalStateException("이미 시작된 게임입니다.");
        if (!room.getPlayers().containsKey(username)) {
            room.addPlayer(username);
        }
        return room;
    }

    public GameRoom getRoom(String roomId) {
        GameRoom room = rooms.get(roomId);
        if (room == null) throw new IllegalArgumentException("존재하지 않는 방입니다.");
        return room;
    }

    public void removeRoom(String roomId) {
        rooms.remove(roomId);
    }

    public boolean roomExists(String roomId) {
        return rooms.containsKey(roomId);
    }

    // 1초마다 타임아웃 체크
    @Scheduled(fixedDelay = 1000)
    public void checkTimeout() {
        for (GameRoom room : rooms.values()) {
            if (!room.isStarted() || room.getTurnStartTime() == null) continue;

            long elapsed = java.time.Duration.between(
                    room.getTurnStartTime(), LocalDateTime.now()
            ).getSeconds();

            long remaining = TURN_SECONDS - elapsed;

            // 타이머 브로드캐스트 (매초)
            RoomMessage timerMsg = new RoomMessage();
            timerMsg.setRoomId(room.getRoomId());
            timerMsg.setType(MessageType.ROOM_TIMER);
            timerMsg.setMessage(String.valueOf(Math.max(0, remaining)));
            timerMsg.setCurrentTurn(room.getCurrentTurnUsername());
            messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), timerMsg);

            // 시간 초과 시 탈락 처리
            if (remaining <= 0) {
                String timedOutUser = room.getCurrentTurnUsername();
                if (timedOutUser == null) continue;

                // 탈락 알림
                RoomMessage wrongMsg = new RoomMessage();
                wrongMsg.setRoomId(room.getRoomId());
                wrongMsg.setSender(timedOutUser);
                wrongMsg.setType(MessageType.ROOM_WRONG);
                wrongMsg.setReason("시간 초과로 탈락했습니다.");
                messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), wrongMsg);

                room.eliminatePlayer(timedOutUser);

                // 생존자 1명이면 종료
                if (room.getAlivePlayers().size() == 1) {
                    RoomMessage endMsg = new RoomMessage();
                    endMsg.setRoomId(room.getRoomId());
                    endMsg.setType(MessageType.ROOM_END);
                    endMsg.setWinner(room.getAlivePlayers().get(0).getUsername());
                    endMsg.setPlayers(room.getPlayers());
                    messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), endMsg);
                    removeRoom(room.getRoomId());
                    return;
                }

                // 다음 차례 안내
                RoomMessage stateMsg = new RoomMessage();
                stateMsg.setRoomId(room.getRoomId());
                stateMsg.setType(MessageType.ROOM_STATE);
                stateMsg.setPlayers(room.getPlayers());
                stateMsg.setCurrentTurn(room.getCurrentTurnUsername());
                stateMsg.setLastWord(room.getLastWord());
                messagingTemplate.convertAndSend("/topic/room/" + room.getRoomId(), stateMsg);
            }
        }
    }
}