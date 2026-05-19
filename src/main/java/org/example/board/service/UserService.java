package org.example.board.service;

import lombok.RequiredArgsConstructor;
import org.example.board.config.JwtProvider;
import org.example.board.dto.LoginRequest;
import org.example.board.dto.UserRequest;
import org.example.board.dto.UserResponse;
import org.example.board.entity.User;
import org.example.board.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public UserResponse join(UserRequest dto) {
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = User.builder()
                .username(dto.getUsername())
                .password(encodedPassword)
                .build();

        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser);
    }

    // 상세 user 조회
    public UserResponse findByUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없음"));
        return new UserResponse(user);
    }

    // 전체 user 조회
    public List<UserResponse> findAllUser() {
        return userRepository.findAll().stream()
                .map(UserResponse::new)
                .toList();
    }

    // 비밀번호 변경
    @Transactional
    public UserResponse updatePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다."));

        // 비밀번호 변경 및 더티 체킹
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);

        return new UserResponse(user); // 변경된 정보 반환
    }

    // user 삭제 (단일)
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // user 삭제 (다중)
    @Transactional
    public void deleteAllUser(List<Long> ids) {
        userRepository.deleteAllByIdInBatch(ids);
    }

    // =================================================================

    public String login(LoginRequest dto) {
        // 아이디 확인
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));

        // 비밀번호 검증
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호 틀림");
        }

        return jwtProvider.createToken(user.getUsername());
    }
}
