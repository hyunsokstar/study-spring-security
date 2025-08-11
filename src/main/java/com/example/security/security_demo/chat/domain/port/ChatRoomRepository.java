package com.example.security.security_demo.chat.domain.port;

import com.example.security.security_demo.chat.domain.ChatRoom;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatRoomRepository {
    ChatRoom save(ChatRoom chatRoom);
    Optional<ChatRoom> findById(UUID id);
    List<ChatRoom> findAll();
}
