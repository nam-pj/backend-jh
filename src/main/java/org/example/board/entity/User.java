package org.example.board.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Builder
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}