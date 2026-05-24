package org.example.board.service;

import lombok.RequiredArgsConstructor;
import org.example.board.dto.BoardRequest;
import org.example.board.dto.BoardResponse;
import org.example.board.entity.Board;
import org.example.board.entity.User;
import org.example.board.repository.BoardRepository;
import org.example.board.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // 전체 게시글 조회
    public List<BoardResponse> findAll() {
        return boardRepository.findAll().stream()
                .map(BoardResponse::new)
                .toList();
    }

    // 상세 게시글 조회
    public BoardResponse findById(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않음"));
        return new BoardResponse(board);
    }

    @Transactional
    public BoardResponse createBoard(BoardRequest dto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없음"));

        Board board = Board.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .user(user)
                .build();

        Board savedBoard = boardRepository.save(board);

        return new BoardResponse(savedBoard);
    }

    @Transactional
    public BoardResponse update(Long id, BoardRequest dto, String username) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        if (!board.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("수정 권한 없음");
        }

        board.update(dto.getTitle(), dto.getContent());

        return new BoardResponse(board);
    }

    @Transactional
    public void delete(Long id, String username) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        if (!board.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("삭제 권한 없음");
        }

        boardRepository.delete(board);
    }
}
