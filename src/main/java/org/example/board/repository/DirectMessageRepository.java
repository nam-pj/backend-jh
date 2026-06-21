package org.example.board.repository;

import org.example.board.entity.DirectMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    // 나와 특정 상대방 사이의 대화 (누가 보냈든 순서 무관하게 시간순)
    List<DirectMessage> findBySenderUsernameAndReceiverUsernameOrSenderUsernameAndReceiverUsernameOrderBySentAtAsc(
            String sender1, String receiver1, String sender2, String receiver2);

    long countByReceiverUsernameAndIsReadFalse(String receiverUsername);
}