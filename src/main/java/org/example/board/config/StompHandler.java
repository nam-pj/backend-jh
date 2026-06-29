package org.example.board.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StompHandler implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            // 핸드셰이크 단계(StompHandshakeInterceptor)에서 저장해둔 username을 꺼냄
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            String username = sessionAttributes != null ? (String) sessionAttributes.get("username") : null;

            if (username == null) {
                throw new IllegalArgumentException("유효하지 않은 연결입니다.");
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, null);
            accessor.setUser(auth);
        }
        return message;
    }
}