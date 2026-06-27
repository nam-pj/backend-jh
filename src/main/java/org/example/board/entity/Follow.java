package org.example.board.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "follow",
        uniqueConstraints = @UniqueConstraint(columnNames = {"follower_username", "following_username"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String followerUsername;   // 팔로우 하는 사람

    @Column(nullable = false)
    private String followingUsername;  // 팔로우 당하는 사람

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Follow(String followerUsername, String followingUsername) {
        this.followerUsername = followerUsername;
        this.followingUsername = followingUsername;
        this.createdAt = LocalDateTime.now();
    }
}