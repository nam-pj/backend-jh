package org.example.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.board.dto.GameRoom;
import org.example.board.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    // 방 생성
    @PostMapping
    public ResponseEntity<GameRoom> createRoom(@AuthenticationPrincipal String username) {
        GameRoom room = roomService.createRoom(username);
        return ResponseEntity.ok(room);
    }

    // 방 입장 (방 존재 여부 확인)
    @GetMapping("/{roomId}")
    public ResponseEntity<GameRoom> getRoom(@PathVariable String roomId) {
        GameRoom room = roomService.getRoom(roomId);
        return ResponseEntity.ok(room);
    }
}