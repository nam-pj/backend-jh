package org.example.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.board.dto.BoardRequest;
import org.example.board.dto.BoardResponse;
import org.example.board.service.BoardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 전체 조회
    @GetMapping("")
    public ResponseEntity<List<BoardResponse>> findAll() {
        return ResponseEntity.ok(boardService.findAll());
    }

    // 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(boardService.findById(id));
    }

    // 게시글 작성
    @PostMapping("")
    public ResponseEntity<BoardResponse> createBoard(
            @RequestBody BoardRequest dto,
            @AuthenticationPrincipal String username) {

        BoardResponse response = boardService.createBoard(dto, username);

        return ResponseEntity.ok(response);
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<BoardResponse> update(
            @PathVariable Long id,
            @RequestBody BoardRequest dto,
            @AuthenticationPrincipal String username) {

        BoardResponse response = boardService.update(id, dto, username);
        return ResponseEntity.ok(response);
    }

    // 게시글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal String username) {
        boardService.delete(id, username);
        return ResponseEntity.noContent().build();
    }
}
