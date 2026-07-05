package org.example.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.board.dto.ChatMessage;
import org.example.board.dto.MessageType;
import org.example.board.entity.GameInvite;
import org.example.board.service.GameInviteService;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameInviteService gameInviteService;

    // 단체(룸) 채팅
    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage chatMessage) {
        chatMessage.setType(MessageType.TALK);

        String destination = "/topic/room/" + chatMessage.getRoomId();
        messagingTemplate.convertAndSend(destination, chatMessage);
    }

    // 1:1 채팅
    @MessageMapping("/chat.private")
    public void sendPrivateMessage(ChatMessage chatMessage, Principal principal) {
        chatMessage.setSender(principal.getName());
        chatMessage.setType(MessageType.TALK);

        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiver(),
                "/queue/private",
                chatMessage
        );
    }

    // 게임 초대 전송
    @MessageMapping("/chat.invite")
    public void sendInvite(ChatMessage chatMessage, Principal principal) {
        String senderUsername = principal.getName();

        GameInvite invite = gameInviteService.createInvite(senderUsername, chatMessage.getReceiver());

        ChatMessage inviteMessage = new ChatMessage();
        inviteMessage.setSender(senderUsername);
        inviteMessage.setReceiver(chatMessage.getReceiver());
        inviteMessage.setType(MessageType.GAME_INVITE);
        inviteMessage.setInviteId(invite.getId());

        messagingTemplate.convertAndSendToUser(
                chatMessage.getReceiver(),
                "/queue/private",
                inviteMessage
        );
    }

    // 게임 초대 수락
    @MessageMapping("/chat.invite.accept")
    public void acceptInvite(ChatMessage chatMessage, Principal principal) {
        String receiverUsername = principal.getName();
        GameInvite invite = gameInviteService.acceptInvite(chatMessage.getInviteId(), receiverUsername);

        // 초대 보낸 사람에게 → BLACK
        ChatMessage senderMsg = new ChatMessage();
        senderMsg.setSender(receiverUsername);
        senderMsg.setType(MessageType.GAME_ACCEPT);
        senderMsg.setInviteId(invite.getId());
        senderMsg.setRoomId(invite.getRoomId());
        senderMsg.setMessage("BLACK"); // 초대 보낸 사람은 BLACK

        messagingTemplate.convertAndSendToUser(
                invite.getSender().getUsername(),
                "/queue/private",
                senderMsg
        );

        // 수락한 사람에게 → WHITE
        ChatMessage receiverMsg = new ChatMessage();
        receiverMsg.setSender(receiverUsername);
        receiverMsg.setType(MessageType.GAME_ACCEPT);
        receiverMsg.setInviteId(invite.getId());
        receiverMsg.setRoomId(invite.getRoomId());
        receiverMsg.setMessage("WHITE"); // 수락한 사람은 WHITE

        messagingTemplate.convertAndSendToUser(
                receiverUsername,
                "/queue/private",
                receiverMsg
        );
    }

    // 게임 초대 거절
    @MessageMapping("/chat.invite.decline")
    public void declineInvite(ChatMessage chatMessage, Principal principal) {
        String receiverUsername = principal.getName();

        GameInvite invite = gameInviteService.declineInvite(chatMessage.getInviteId(), receiverUsername);

        ChatMessage resultMessage = new ChatMessage();
        resultMessage.setSender(receiverUsername);
        resultMessage.setType(MessageType.GAME_DECLINE);
        resultMessage.setInviteId(invite.getId());

        messagingTemplate.convertAndSendToUser(
                invite.getSender().getUsername(),
                "/queue/private",
                resultMessage
        );
    }

    @MessageMapping("/game.move")
    public void gameMove(ChatMessage chatMessage, Principal principal) {
        chatMessage.setSender(principal.getName());
        messagingTemplate.convertAndSend(
                "/topic/room/" + chatMessage.getRoomId(),
                chatMessage
        );
    }

    // 이 컨트롤러에서 발생한 예외를 요청을 보낸 사람에게 다시 알려줌
    // (안 하면 서버 로그에만 찍히고 클라이언트는 아무 반응 없음)
    @MessageExceptionHandler
    public void handleException(Throwable exception, Principal principal) {
        if (principal == null) {
            return;
        }

        ChatMessage errorMessage = new ChatMessage();
        errorMessage.setType(MessageType.ERROR);
        errorMessage.setMessage(exception.getMessage());

        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/errors",
                errorMessage
        );
    }
}