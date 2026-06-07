package org.example.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.board.dto.UserRoleUpdateRequest;
import org.example.board.dto.UserRoleUpdateResponse;
import org.example.board.entity.User;
import org.example.board.repository.UserRepository;
import org.example.board.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @PatchMapping("/{userId}/role")
    public ResponseEntity<UserRoleUpdateResponse> updateUserRole(
            @PathVariable Long userId,
            @RequestBody UserRoleUpdateRequest requestDto) {

        UserRoleUpdateResponse responseDto = adminService.updateUserRole(userId, requestDto.getRole());

        return ResponseEntity.ok(responseDto);
    }
}