package com.example.security.security_demo.chatting.service;

import com.example.security.security_demo.chatting.domain.ChattingRoom;
import com.example.security.security_demo.chatting.infrastructure.ChattingMessageRepository;
import com.example.security.security_demo.chatting.infrastructure.ChattingRoomRepository;
import com.example.security.security_demo.chatting.presentation.dto.ChattingRoomResponse;
import com.example.security.security_demo.chatting.presentation.dto.CreateChattingRoomRequest;
import com.example.security.security_demo.user.domain.User;
import com.example.security.security_demo.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public ChattingRoomResponse createRoom(CreateChattingRoomRequest req, String username) {
        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        ChattingRoom room = new ChattingRoom(req.getName(), creator);
        ChattingRoom saved = roomRepository.saveAndFlush(room);
        
        ChattingRoomResponse.CreatorInfo creatorInfo = new ChattingRoomResponse.CreatorInfo(
            saved.getCreator().getId(), 
            saved.getCreator().getUsername()
        );
        
        return ChattingRoomResponse.of(saved.getId(), saved.getName(), creatorInfo, saved.getCreatedAt(), 0L, null);
    }

    @Transactional
    public ChattingRoomResponse createRoom(CreateChattingRoomRequest req, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + creatorId));
        
        ChattingRoom room = new ChattingRoom(req.getName(), creator);
        ChattingRoom saved = roomRepository.saveAndFlush(room);
        
        ChattingRoomResponse.CreatorInfo creatorInfo = new ChattingRoomResponse.CreatorInfo(
            saved.getCreator().getId(), 
            saved.getCreator().getUsername()
        );
        
        return ChattingRoomResponse.of(saved.getId(), saved.getName(), creatorInfo, saved.getCreatedAt(), 0L, null);
    }

    @Transactional
    public ChattingRoomResponse createRoom(CreateChattingRoomRequest req) {
        // 기본 사용자로 생성 (임시)
        return createRoom(req, 1L);
    }

    public List<ChattingRoomResponse> listRooms() {
        return roomRepository.findAll().stream().map(r -> {
            long count = messageRepository.countByRoomId(r.getId());
            var lastAt = messageRepository.findLastMessageAt(r.getId()).orElse(null);
            
            // 개설자 정보 구성
            ChattingRoomResponse.CreatorInfo creatorInfo = null;
            if (r.getCreator() != null) {
                creatorInfo = new ChattingRoomResponse.CreatorInfo(
                    r.getCreator().getId(), 
                    r.getCreator().getUsername()
                );
            } else {
                // 개설자 정보가 없는 경우 기본값 설정
                creatorInfo = new ChattingRoomResponse.CreatorInfo(null, "Unknown");
            }
            
            return ChattingRoomResponse.of(r.getId(), r.getName(), creatorInfo, r.getCreatedAt(), count, lastAt);
        }).toList();
    }
}