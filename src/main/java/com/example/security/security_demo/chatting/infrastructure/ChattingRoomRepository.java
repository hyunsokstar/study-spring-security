package com.example.security.security_demo.chatting.infrastructure;

import com.example.security.security_demo.chatting.domain.ChattingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChattingRoomRepository extends JpaRepository<ChattingRoom, UUID> {
}