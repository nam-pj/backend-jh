package org.example.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.board.entity.Follow;
import org.example.board.entity.User;
import org.example.board.repository.FollowRepository;
import org.example.board.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@ResponseBody
@RequiredArgsConstructor
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @GetMapping("/search")
    public ResponseEntity<?> searchByUsername(@RequestParam String username, Principal principal) {

        // principal이 null인지 먼저 확인
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("message", "인증이 필요합니다."));
        }

        if (username.equals(principal.getName())) {
            return ResponseEntity.badRequest().body(Map.of("message", "본인은 검색할 수 없습니다."));
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    boolean isFollowing = followRepository
                            .findByFollowerUsernameAndFollowingUsername(
                                    principal.getName(), user.getUsername())
                            .isPresent();

                    return ResponseEntity.ok(Map.of(
                            "username", user.getUsername(),
                            "isFollowing", isFollowing
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 팔로우
    @PostMapping("/{targetUsername}")
    public ResponseEntity<?> follow(@PathVariable String targetUsername, Principal principal) {

        String myUsername = principal.getName();

        // 이미 팔로우 중이면 무시
        if (followRepository.findByFollowerUsernameAndFollowingUsername(myUsername, targetUsername).isPresent()) {
            return ResponseEntity.ok(Map.of("message", "이미 팔로우 중입니다."));
        }

        followRepository.save(
                Follow.builder()
                        .followerUsername(myUsername)
                        .followingUsername(targetUsername)
                        .build()
        );

        return ResponseEntity.ok(Map.of("message", "팔로우 완료"));
    }

    // 언팔로우
    @DeleteMapping("/{targetUsername}")
    public ResponseEntity<?> unfollow(@PathVariable String targetUsername, Principal principal) {

        followRepository.deleteByFollowerUsernameAndFollowingUsername(
                principal.getName(), targetUsername);

        return ResponseEntity.ok(Map.of("message", "언팔로우 완료"));
    }

    // 내가 팔로우하는 목록
    @GetMapping("/following")
    public ResponseEntity<?> getFollowing(Principal principal) {

        List<String> following = followRepository
                .findByFollowerUsername(principal.getName())
                .stream()
                .map(Follow::getFollowingUsername)
                .toList();

        return ResponseEntity.ok(following);
    }
}