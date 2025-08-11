package com.example.security.security_demo.chat.domain.port;

import com.example.security.security_demo.chat.domain.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository {
    Message save(Message message);
    List<Message> findByRoomIdOrderByCreatedAtAsc(UUID roomId);
}
