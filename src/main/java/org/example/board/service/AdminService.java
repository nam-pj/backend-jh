package org.example.board.service;

import lombok.RequiredArgsConstructor;
import org.example.board.dto.UserRoleUpdateResponse;
import org.example.board.entity.User;
import org.example.board.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Transactional
    public UserRoleUpdateResponse updateUserRole(Long userId, String newRole) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없음."));

        user.updateRole(newRole);

        return new UserRoleUpdateResponse(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );
    }
}