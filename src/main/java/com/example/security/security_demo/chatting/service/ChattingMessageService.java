package com.example.security.security_demo.chatting.service;

import com.example.security.security_demo.chatting.domain.ChattingMessage;
import com.example.security.security_demo.chatting.domain.ChattingRoom;
import com.example.security.security_demo.chatting.infrastructure.ChattingMessageRepository;
import com.example.security.security_demo.chatting.infrastructure.ChattingRoomRepository;
import com.example.security.security_demo.chatting.presentation.dto.ChattingMessageResponse;
import com.example.security.security_demo.chatting.presentation.dto.SendChattingMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChattingMessageService {

    private final ChattingRoomRepository roomRepository;
    private final ChattingMessageRepository messageRepository;

    @Transactional
    public ChattingMessageResponse sendMessage(UUID roomId, SendChattingMessageRequest req) {
        ChattingRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));
        var saved = messageRepository.save(
                ChattingMessage.builder()
                        .room(room)
                        .sender(req.getSender())
                        .content(req.getContent())
                        .build()
        );
        return ChattingMessageResponse.of(saved.getId(), room.getId(), saved.getSender(), saved.getContent(), saved.getCreatedAt());
    }

    public Page<ChattingMessageResponse> listMessages(UUID roomId, int page, int size) {
        return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId, PageRequest.of(page, size))
                .map(m -> ChattingMessageResponse.of(m.getId(), roomId, m.getSender(), m.getContent(), m.getCreatedAt()));
    }
}