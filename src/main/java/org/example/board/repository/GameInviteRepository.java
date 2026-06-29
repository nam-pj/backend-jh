package org.example.board.repository;

import org.example.board.entity.GameInvite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameInviteRepository extends JpaRepository<GameInvite, Long> {
}