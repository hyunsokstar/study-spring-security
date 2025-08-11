package com.example.security.security_demo.chat.application;

import com.example.security.security_demo.chat.domain.ChatRoom;
import com.example.security.security_demo.chat.domain.Message;
import com.example.security.security_demo.chat.domain.port.ChatRoomRepository;
import com.example.security.security_demo.chat.domain.port.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    public ChatRoom getRoom(UUID roomId) {
        return chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + roomId));
    }

    public List<ChatRoom> listRooms() {
        return chatRoomRepository.findAll();
    }

    public List<Message> getMessages(UUID roomId) {
        return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
    }
}
