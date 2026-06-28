package org.example.board.repository;

import org.example.board.entity.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    // 특정 두 유저 사이의 대화 내역 (시간순)
    List<DirectMessage> findBySenderUsernameAndReceiverUsernameOrSenderUsernameAndReceiverUsernameOrderBySentAtAsc(
            String sender1, String receiver1, String sender2, String receiver2);

    // 내가 주고받은 모든 메시지 (최신순) - 대화 목록용
    List<DirectMessage> findBySenderUsernameOrReceiverUsernameOrderBySentAtDesc(
            String senderUsername, String receiverUsername);

    // 안 읽은 메시지 개수
    long countByReceiverUsernameAndIsReadFalse(String receiverUsername);

    List<DirectMessage> findByReceiverUsernameAndSenderUsernameAndIsReadFalse(
            String receiverUsername, String senderUsername);
}