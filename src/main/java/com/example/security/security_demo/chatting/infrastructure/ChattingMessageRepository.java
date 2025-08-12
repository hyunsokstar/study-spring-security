package com.example.security.security_demo.chatting.infrastructure;

import com.example.security.security_demo.chatting.domain.ChattingMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ChattingMessageRepository extends JpaRepository<ChattingMessage, UUID> {

    Page<ChattingMessage> findByRoomIdOrderByCreatedAtAsc(UUID roomId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM ChattingMessage m WHERE m.room.id = :roomId")
    long countByRoomId(@Param("roomId") UUID roomId);

    Optional<ChattingMessage> findTop1ByRoomIdOrderByCreatedAtDesc(UUID roomId);

    // 필요 시 마지막 메시지 시간만 뽑을 때 사용 (LocalDateTime 반환)
    @Query("SELECT MAX(m.createdAt) FROM ChattingMessage m WHERE m.room.id = :roomId")
    Optional<LocalDateTime> findLastMessageAt(@Param("roomId") UUID roomId);
    
    // JPQL 사용 - 명확한 삭제 조건 표현으로 가독성 향상
    @Modifying
    @Query("DELETE FROM ChattingMessage m WHERE m.room.id = :roomId")
    void deleteByRoomId(@Param("roomId") UUID roomId);
}