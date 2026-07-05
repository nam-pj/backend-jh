package org.example.board.controller;

import lombok.RequiredArgsConstructor;
import org.example.board.entity.DirectMessage;
import org.example.board.dto.DmConversationResponse;
import org.example.board.dto.DmResponse;
import org.example.board.dto.DmSendRequest;
import org.example.board.entity.InviteStatus;
import org.example.board.repository.DirectMessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@ResponseBody
@RequiredArgsConstructor
@RequestMapping("/api/dm")
public class DmController {

    private final DirectMessageRepository directMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ================================
    // STOMP - 실시간 메시지 전송
    // ================================

    @MessageMapping("/dm/send")
    public void sendDm(DmSendRequest request, Principal principal) {
        System.out.println("받은 type: " + request.type());
        System.out.println("받은 roomId: " + request.roomId());
        String senderUsername = principal.getName();

        DirectMessage saved = directMessageRepository.save(
                DirectMessage.builder()
                        .senderUsername(senderUsername)
                        .receiverUsername(request.receiverUsername())
                        .content(request.content())
                        .type(request.type() != null ? request.type() : "TALK")
                        .status(InviteStatus.PENDING)  // 추가
                        .roomId(request.roomId())
                        .sentAt(LocalDateTime.now())
                        .isRead(false)
                        .build()
        );

        DmResponse response = DmResponse.from(saved);

        // 받는 사람에게 실시간 전송
        messagingTemplate.convertAndSendToUser(
                request.receiverUsername(),
                "/queue/dm",
                response
        );

        // 보낸 사람 화면에도 echo
        messagingTemplate.convertAndSendToUser(
                senderUsername,
                "/queue/dm",
                response
        );
    }

    // ================================
    // REST - 대화 내역 / 목록 / 뱃지
    // ================================

    // 특정 상대방과의 대화 내역
    @GetMapping("/{otherUsername}")
    public List<DmResponse> getConversation(@PathVariable String otherUsername, Principal principal) {

        String myUsername = principal.getName();

        return directMessageRepository
                .findBySenderUsernameAndReceiverUsernameOrSenderUsernameAndReceiverUsernameOrderBySentAtAsc(
                        myUsername, otherUsername, otherUsername, myUsername)
                .stream()
                .map(DmResponse::from)
                .toList();
    }

    // 나와 대화한 상대 목록 + 마지막 메시지
    @GetMapping("/conversations")
    public List<DmConversationResponse> getConversations(Principal principal) {

        String myUsername = principal.getName();

        List<DirectMessage> all = directMessageRepository
                .findBySenderUsernameOrReceiverUsernameOrderBySentAtDesc(myUsername, myUsername);

        Map<String, DirectMessage> latestMap = new LinkedHashMap<>();

        for (DirectMessage msg : all) {
            String other = msg.getSenderUsername().equals(myUsername)
                    ? msg.getReceiverUsername()
                    : msg.getSenderUsername();

            latestMap.putIfAbsent(other, msg);
        }

        return latestMap.entrySet().stream()
                .map(e -> new DmConversationResponse(
                        e.getKey(),
                        e.getValue().getContent(),
                        e.getValue().getSentAt()))
                .toList();
    }

    // 안 읽은 메시지 개수
    @GetMapping("/unread-count")
    public long getUnreadCount(Principal principal) {
        return directMessageRepository.countByReceiverUsernameAndIsReadFalse(principal.getName());
    }
    @PatchMapping("/{senderUsername}/read")
    public ResponseEntity<?> markAsRead(@PathVariable String senderUsername, Principal principal) {

        List<DirectMessage> unreadMessages = directMessageRepository
                .findByReceiverUsernameAndSenderUsernameAndIsReadFalse(
                        principal.getName(), senderUsername);

        unreadMessages.forEach(DirectMessage::markAsRead);
        directMessageRepository.saveAll(unreadMessages);

        return ResponseEntity.ok(Map.of("message", "읽음 처리 완료"));
    }

    @PatchMapping("/{messageId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long messageId,
                                          @RequestParam InviteStatus status,
                                          Principal principal) {
        return directMessageRepository.findById(messageId)
                .map(msg -> {
                    msg.updateStatus(status);
                    directMessageRepository.save(msg);
                    return ResponseEntity.ok(Map.of("message", "상태 업데이트 완료"));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}