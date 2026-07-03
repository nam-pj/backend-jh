package org.example.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.board.dto.*;
import org.example.board.service.RoomService;
import org.example.board.service.WordValidationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
public class GameRoomController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;
    private final WordValidationService wordValidationService;

    private void broadcast(String roomId, RoomMessage msg) {
        messagingTemplate.convertAndSend("/topic/room/" + roomId, msg);
    }

    // 방 입장
    @MessageMapping("/room.join")
    public void joinRoom(RoomMessage msg, Principal principal) {
        String username = principal.getName();
        GameRoom room = roomService.joinRoom(msg.getRoomId(), username);

        RoomMessage response = new RoomMessage();
        response.setRoomId(msg.getRoomId());
        response.setSender(username);
        response.setType(MessageType.ROOM_JOIN);
        response.setPlayers(room.getPlayers());
        broadcast(msg.getRoomId(), response);
    }

    // 게임 시작 (방장만)
    @MessageMapping("/room.start")
    public void startGame(RoomMessage msg, Principal principal) {
        String username = principal.getName();
        GameRoom room = roomService.getRoom(msg.getRoomId());

        if (!room.getHostUsername().equals(username)) return;
        if (room.getPlayers().size() < 2) return;

        room.setStarted(true);
        room.setTurnOrder(new ArrayList<>(room.getPlayers().keySet()));
        room.setCurrentTurnIndex(0);
        room.setLastWord("");
        room.setUsedWords(new ArrayList<>());
        room.setTurnStartTime(LocalDateTime.now());

        RoomMessage response = new RoomMessage();
        response.setRoomId(msg.getRoomId());
        response.setType(MessageType.ROOM_START);
        response.setPlayers(room.getPlayers());
        response.setCurrentTurn(room.getCurrentTurnUsername());
        response.setLastWord("");
        broadcast(msg.getRoomId(), response);
    }

    // 단어 입력
    @MessageMapping("/room.word")
    public void submitWord(RoomMessage msg, Principal principal) {
        String username = principal.getName();
        GameRoom room = roomService.getRoom(msg.getRoomId());

        if (!room.isStarted()) return;
        if (!username.equals(room.getCurrentTurnUsername())) return;

        String word = msg.getMessage().trim();
        if (word.isEmpty()) return;

        // ✅ 한 글자 차단 (백엔드에서도 검증)
        if (word.length() < 2) {
            sendWrong(msg.getRoomId(), username, word, "두 글자 이상 입력해야 합니다.");
            return; // 탈락 처리 없이 재입력 기회 부여
        }

        // 1. 이미 사용된 단어 체크
        if (room.getUsedWords().contains(word)) {
            sendWrong(msg.getRoomId(), username, word, "이미 사용된 단어입니다.");
            eliminate(room, msg.getRoomId(), username);
            return;
        }

        // 2. 끝말잇기 규칙 체크
        if (!room.getLastWord().isEmpty()) {
            char lastChar = room.getLastWord().charAt(room.getLastWord().length() - 1);
            char firstChar = word.charAt(0);
            if (lastChar != firstChar) {
                sendWrong(msg.getRoomId(), username, word,
                        "'" + lastChar + "'(으)로 시작해야 합니다.");
                eliminate(room, msg.getRoomId(), username);
                return;
            }
        }

        // 3. 실제 존재하는 단어인지 체크 (국립국어원 API)
        if (!wordValidationService.isValidWord(word)) {
            sendWrong(msg.getRoomId(), username, word, "존재하지 않는 단어입니다.");
            return;
        }

        // 정상 처리
        room.getUsedWords().add(word);
        room.setLastWord(word);
        room.nextTurn();

        RoomMessage response = new RoomMessage();
        response.setRoomId(msg.getRoomId());
        response.setSender(username);
        response.setType(MessageType.ROOM_WORD);
        response.setMessage(word);
        response.setLastWord(word);
        response.setCurrentTurn(room.getCurrentTurnUsername());
        response.setUsedWords(room.getUsedWords());
        response.setPlayers(room.getPlayers());
        broadcast(msg.getRoomId(), response);
    }

    // 탈락 처리
    private void eliminate(GameRoom room, String roomId, String username) {
        room.eliminatePlayer(username);

        // 생존자 1명이면 게임 종료
        if (room.getAlivePlayers().size() == 1) {
            RoomMessage endMsg = new RoomMessage();
            endMsg.setRoomId(roomId);
            endMsg.setType(MessageType.ROOM_END);
            endMsg.setWinner(room.getAlivePlayers().get(0).getUsername());
            endMsg.setPlayers(room.getPlayers());
            broadcast(roomId, endMsg);
            roomService.removeRoom(roomId);
            return;
        }

        // 다음 차례 안내
        RoomMessage stateMsg = new RoomMessage();
        stateMsg.setRoomId(roomId);
        stateMsg.setType(MessageType.ROOM_STATE);
        stateMsg.setPlayers(room.getPlayers());
        stateMsg.setCurrentTurn(room.getCurrentTurnUsername());
        stateMsg.setLastWord(room.getLastWord());
        broadcast(roomId, stateMsg);
    }

    private void sendWrong(String roomId, String username, String word, String reason) {
        RoomMessage wrongMsg = new RoomMessage();
        wrongMsg.setRoomId(roomId);
        wrongMsg.setSender(username);
        wrongMsg.setType(MessageType.ROOM_WRONG);
        wrongMsg.setMessage(word);
        wrongMsg.setReason(reason);
        broadcast(roomId, wrongMsg);
    }

    // 방 퇴장
    @MessageMapping("/room.leave")
    public void leaveRoom(RoomMessage msg, Principal principal) {
        String username = principal.getName();

        try {
            GameRoom room = roomService.getRoom(msg.getRoomId());
            room.removePlayer(username);

            // 1. 일반 퇴장 알림 발송 (기존 유지)
            RoomMessage response = new RoomMessage();
            response.setRoomId(msg.getRoomId());
            response.setSender(username);
            response.setType(MessageType.ROOM_LEAVE);
            response.setPlayers(room.getPlayers());
            broadcast(msg.getRoomId(), response);

            // 2. 방에 아무도 없으면 방 삭제 후 종료
            if (room.getPlayers().isEmpty()) {
                roomService.removeRoom(msg.getRoomId());
                return;
            }

            if (room.isStarted()) {

                // 남은 플레이어 중 생존자(alive == true) 수 계산
                long aliveCount = room.getPlayers().values().stream()
                        .filter(p -> p.isAlive())
                        .count();

                // 남은 사람이 1명 이하라면 게임 종료 처리
                if (aliveCount <= 1) {
                    room.setStarted(false);

                    // 최후의 생존자 이름 찾기
                    String winnerName = room.getPlayers().values().stream()
                            .filter(p -> p.isAlive())
                            .map(p -> p.getUsername())
                            .findFirst()
                            .orElse("기권승");

                    // 프론트엔드로 게임 종료(ROOM_END) 브로드캐스트 발송
                    RoomMessage endMsg = new RoomMessage();
                    endMsg.setRoomId(msg.getRoomId());
                    endMsg.setType(MessageType.ROOM_END);
                    endMsg.setWinner(winnerName);
                    endMsg.setPlayers(room.getPlayers());

                    broadcast(msg.getRoomId(), endMsg);
                }
            }
        } catch (Exception ignored) {}
    }
}