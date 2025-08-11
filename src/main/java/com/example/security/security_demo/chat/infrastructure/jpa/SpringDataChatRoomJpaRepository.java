package com.example.security.security_demo.chat.infrastructure.jpa;

import com.example.security.security_demo.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataChatRoomJpaRepository extends JpaRepository<ChatRoom, UUID> {
}
