package org.example.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.board.entity.DirectMessage;
import org.example.board.dto.DmResponse;
import org.example.board.dto.DmSendRequest;
import org.example.board.repository.DirectMessageRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class DmStompController {

    private final DirectMessageRepository directMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/dm/send")
    public void sendDm(DmSendRequest request, Principal principal) {

        String senderUsername = principal.getName();

        // 1. DB 저장 (받는 사람이 접속 중이 아니어도 메시지는 남아있어야 하니까)
        DirectMessage saved = directMessageRepository.save(
                DirectMessage.builder()
                        .senderUsername(senderUsername)
                        .receiverUsername(request.receiverUsername())
                        .content(request.content())
                        .build()
        );

        DmResponse response = DmResponse.from(saved);

        // 2. 받는 사람에게 실시간 전송
        messagingTemplate.convertAndSendToUser(
                request.receiverUsername(),
                "/queue/dm",
                response
        );

        // 3. 보낸 사람 화면에도 echo (다른 탭/기기 동기화용)
        messagingTemplate.convertAndSendToUser(
                senderUsername,
                "/queue/dm",
                response
        );
    }
}