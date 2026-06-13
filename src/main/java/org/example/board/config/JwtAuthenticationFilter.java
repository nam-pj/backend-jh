package org.example.board.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 헤더에서 토큰 꺼내기
        String token = request.getHeader("Authorization");

        // 토큰이 있고 유효하다면 유저 정보 추출
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            String username = jwtProvider.getUsername(jwt);
            String role = jwtProvider.getRole(jwt);

            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

            // 스프링 시큐리티에게 "이 유저 인증됨"이라고 알려주기
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}