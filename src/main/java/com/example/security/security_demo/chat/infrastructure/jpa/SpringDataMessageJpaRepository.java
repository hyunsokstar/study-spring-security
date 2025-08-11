package com.example.security.security_demo.chat.infrastructure.jpa;

import com.example.security.security_demo.chat.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SpringDataMessageJpaRepository extends JpaRepository<Message, UUID> {
    List<Message> findByRoomIdOrderByCreatedAtAsc(UUID roomId);
}
