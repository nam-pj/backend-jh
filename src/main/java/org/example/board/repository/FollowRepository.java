package org.example.board.repository;

import org.example.board.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 내가 팔로우하는 사람 목록
    List<Follow> findByFollowerUsername(String followerUsername);

    // 팔로우 여부 확인
    Optional<Follow> findByFollowerUsernameAndFollowingUsername(
            String followerUsername, String followingUsername);

    // 팔로우 취소
    void deleteByFollowerUsernameAndFollowingUsername(
            String followerUsername, String followingUsername);
}