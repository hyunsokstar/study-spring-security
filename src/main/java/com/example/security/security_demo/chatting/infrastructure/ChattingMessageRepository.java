package com.example.security.security_demo.chatting.infrastructure;

import com.example.security.security_demo.chatting.domain.ChattingMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ChattingMessageRepository extends JpaRepository<ChattingMessage, UUID> {

    Page<ChattingMessage> findByRoomIdOrderByCreatedAtAsc(UUID roomId, Pageable pageable);

    long countByRoomId(UUID roomId);

    Optional<ChattingMessage> findTop1ByRoomIdOrderByCreatedAtDesc(UUID roomId);

    // 필요 시 마지막 메시지 시간만 뽑을 때 사용
    default Optional<Instant> findLastMessageAt(UUID roomId) {
        return findTop1ByRoomIdOrderByCreatedAtDesc(roomId).map(ChattingMessage::getCreatedAt);
    }
}