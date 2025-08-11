package com.example.security.security_demo.chat.infrastructure.jpa;

import com.example.security.security_demo.chat.domain.Message;
import com.example.security.security_demo.chat.domain.port.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MessageJpaAdapter implements MessageRepository {

    private final SpringDataMessageJpaRepository jpaRepository;

    @Override
    public Message save(Message message) {
        return jpaRepository.save(message);
    }

    @Override
    public List<Message> findByRoomIdOrderByCreatedAtAsc(UUID roomId) {
        return jpaRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
    }
}
