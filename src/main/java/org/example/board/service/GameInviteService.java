package org.example.board.service;

import lombok.RequiredArgsConstructor;
import org.example.board.entity.GameInvite;
import org.example.board.entity.InviteStatus;
import org.example.board.entity.User;
import org.example.board.repository.GameInviteRepository;
import org.example.board.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GameInviteService {

    private final GameInviteRepository gameInviteRepository;
    private final UserRepository userRepository;

    // 초대 생성
    @Transactional
    public GameInvite createInvite(String senderUsername, String receiverUsername) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new IllegalArgumentException("보낸 사람을 찾을 수 없음"));
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new IllegalArgumentException("받는 사람을 찾을 수 없음"));

        GameInvite invite = GameInvite.builder()
                .sender(sender)
                .receiver(receiver)
                .status(InviteStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        return gameInviteRepository.save(invite);
    }

    // 초대 수락
    @Transactional
    public GameInvite acceptInvite(Long inviteId, String receiverUsername) {
        GameInvite invite = getValidInvite(inviteId, receiverUsername);
        invite.accept(); // 내부에서 roomId 생성됨
        invite.getSender().getUsername();
        return invite;
    }

    // 초대 거절
    @Transactional
    public GameInvite declineInvite(Long inviteId, String receiverUsername) {
        GameInvite invite = getValidInvite(inviteId, receiverUsername);
        invite.decline();

        invite.getSender().getUsername();

        return invite;
    }

    // 조회 + 권한/상태 검증 공통 로직
    private GameInvite getValidInvite(Long inviteId, String receiverUsername) {
        GameInvite invite = gameInviteRepository.findById(inviteId)
                .orElseThrow(() -> new IllegalArgumentException("초대를 찾을 수 없음"));

        if (!invite.getReceiver().getUsername().equals(receiverUsername)) {
            throw new IllegalArgumentException("응답 권한이 없음");
        }

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 초대임");
        }

        return invite;
    }
}