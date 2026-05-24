package org.example.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRequest {

    @NotBlank(message = "아이디는 필수 입력값")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력값")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상")
    private String password;
}
