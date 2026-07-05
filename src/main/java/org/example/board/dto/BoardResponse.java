package org.example.board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.board.entity.Board;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class BoardResponse {
    private Long id;
    private String title;
    private String content;
    private String username;
    private LocalDateTime createdAt;

    // 엔티티를 DTO로 변환하는 생성자
    public BoardResponse(Board board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.username = board.getUser().getUsername();
        this.createdAt = board.getCreatedAt();
    }
}