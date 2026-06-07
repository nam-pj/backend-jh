package org.example.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRoleUpdateResponse {
    private Long userId;
    private String username;
    private String updatedRole;
}