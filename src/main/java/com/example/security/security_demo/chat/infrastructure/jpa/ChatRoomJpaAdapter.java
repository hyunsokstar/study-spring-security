package com.example.security.security_demo.chat.infrastructure.jpa;

import com.example.security.security_demo.chat.domain.ChatRoom;
import com.example.security.security_demo.chat.domain.port.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ChatRoomJpaAdapter implements ChatRoomRepository {

    private final SpringDataChatRoomJpaRepository jpaRepository;

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        return jpaRepository.save(chatRoom);
    }

    @Override
    public Optional<ChatRoom> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ChatRoom> findAll() {
        return jpaRepository.findAll();
    }
}
