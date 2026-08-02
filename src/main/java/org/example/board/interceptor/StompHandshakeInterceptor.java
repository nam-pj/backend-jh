package org.example.board.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.board.config.JwtProvider;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
@Component
@RequiredArgsConstructor
public class StompHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpRequest = servletRequest.getServletRequest();
            Cookie[] cookies = httpRequest.getCookies();

            System.out.println("핸드셰이크 시도 - 쿠키 수: " + (cookies != null ? cookies.length : 0));

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    System.out.println("쿠키: " + cookie.getName() + " = " + cookie.getValue().substring(0, Math.min(10, cookie.getValue().length())) + "...");
                    if ("token".equals(cookie.getName())) {
                        try {
                            String username = jwtProvider.getUsername(cookie.getValue());
                            System.out.println("핸드셰이크 인증 성공: " + username);
                            attributes.put("username", username);
                            return true;
                        } catch (Exception e) {
                            System.out.println("핸드셰이크 인증 실패: " + e.getMessage());
                            return false;
                        }
                    }
                }
            }

            String token = httpRequest.getParameter("token");
            if (token != null) {
                try {
                    String username = jwtProvider.getUsername(token);
                    System.out.println("쿼리파라미터 인증 성공: " + username);
                    attributes.put("username", username);
                    return true;
                } catch (Exception e) {
                    System.out.println("쿼리파라미터 인증 실패: " + e.getMessage());
                    return false;
                }
            }

            System.out.println("토큰 없음 - 핸드셰이크 거부");
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}