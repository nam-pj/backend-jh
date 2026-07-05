package org.example.board.config;

import lombok.RequiredArgsConstructor;
import org.example.board.entity.User;
import org.example.board.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User rootAdmin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("1234"))
                    .role("ROLE_ADMIN")
                    .build();

            userRepository.save(rootAdmin);
        }
    }
}