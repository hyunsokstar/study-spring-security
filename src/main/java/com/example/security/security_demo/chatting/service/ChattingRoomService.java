package com.example.security.security_demo.chatting.service;

import com.example.security.security_demo.chatting.domain.ChattingRoom;
import com.example.security.security_demo.chatting.infrastructure.ChattingMessageRepository;
import com.example.security.security_demo.chatting.infrastructure.ChattingRoomRepository;
import com.example.security.security_demo.chatting.presentation.dto.ChattingRoomResponse;
import com.example.security.security_demo.chatting.presentation.dto.CreateChattingRoomRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChattingRoomService {

    private final ChattingRoomRepository roomRepository;
    private final ChattingMessageRepository messageRepository;

    @Transactional
    public ChattingRoomResponse createRoom(CreateChattingRoomRequest req) {
        ChattingRoom room = new ChattingRoom(req.getName());
        ChattingRoom saved = roomRepository.saveAndFlush(room); // flush 즉시
        return ChattingRoomResponse.of(saved.getId(), saved.getName(), saved.getCreatedAt(), 0L, null);
    }

    public List<ChattingRoomResponse> listRooms() {
        return roomRepository.findAll().stream().map(r -> {
            long count = messageRepository.countByRoomId(r.getId());
            var lastAt = messageRepository.findLastMessageAt(r.getId()).orElse(null);
            return ChattingRoomResponse.of(r.getId(), r.getName(), r.getCreatedAt(), count, lastAt);
        }).toList();
    }
}