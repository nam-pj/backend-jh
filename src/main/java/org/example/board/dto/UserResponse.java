package org.example.board.dto;

import org.example.board.entity.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private final Long id;
    private final String username;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
    }
}