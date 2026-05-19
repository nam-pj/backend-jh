package org.example.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.board.dto.LoginRequest;
import org.example.board.dto.UserRequest;
import org.example.board.dto.UserResponse;
import org.example.board.dto.UserUpdateRequest;
import org.example.board.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 사용자 조회(다건)
    @GetMapping("")
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAllUser());
    }

    // 사용자 조회(단건)
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findByUser(id));
    }

    // 사용자 등록
    @PostMapping("")
    public ResponseEntity<UserResponse> signup(@RequestBody UserRequest dto) {
        UserResponse response = userService.join(dto);
        return ResponseEntity.ok(response);
    }

    // 사용자 수정(비밀번호)
    @PatchMapping("/{id}/password")
    public ResponseEntity<UserResponse> updatePassword(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest dto) {

        userService.updatePassword(id, dto.getNewPassword());

        UserResponse response = userService.findByUser(id);
        return ResponseEntity.ok(response);
    }

    // 사용자 삭제(단건)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // 사용자 삭제(다건)
    @DeleteMapping("")
    public ResponseEntity<Void> deleteAllUser(@RequestBody List<Long> ids) {
        userService.deleteAllUser(ids);
        return ResponseEntity.noContent().build();
    }

    // =================================================================

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest dto) {
        String token = userService.login(dto);
        return ResponseEntity.ok(token); // 성공 시 JWT 토큰 문자열 반환
    }
}