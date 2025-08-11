package com.example.security.security_demo.chat.application;

import com.example.security.security_demo.chat.domain.ChatRoom;
import com.example.security.security_demo.chat.domain.Message;
import com.example.security.security_demo.chat.domain.port.ChatRoomRepository;
import com.example.security.security_demo.chat.domain.port.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;

    public ChatRoom createRoom(String name) {
        ChatRoom room = ChatRoom.create(name);
        return chatRoomRepository.save(room);
    }

    public Message postMessage(UUID roomId, String senderUserId, String content) {
        // 단순 검증: 방 존재 확인
        chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("ChatRoom not found: " + roomId));
        Message message = Message.of(roomId, senderUserId, content);
        return messageRepository.save(message);
    }
}
