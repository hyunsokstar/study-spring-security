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

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

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
            
            // LocalDateTime을 Instant로 변환
            var lastAtInstant = (lastAt != null) ? lastAt.toInstant(ZoneOffset.UTC) : null;
            
            return ChattingRoomResponse.of(r.getId(), r.getName(), creatorInfo, r.getCreatedAt(), Long.valueOf(count), lastAtInstant);
        }).toList();
    }

    @Transactional
    public void deleteRoom(UUID roomId, String username) {
        // 채팅방 존재 여부 확인
        ChattingRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
        
        // 현재 사용자 조회
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 방장 권한 검증
        if (room.getCreator() == null || !room.getCreator().getId().equals(currentUser.getId())) {
            throw new SecurityException("채팅방을 삭제할 권한이 없습니다. 방장만 삭제할 수 있습니다.");
        }
        
        // 연관된 메시지들도 함께 삭제 (CASCADE로 처리되지 않는 경우를 대비)
        messageRepository.deleteByRoomId(roomId);
        
        // 채팅방 삭제
        roomRepository.delete(room);
    }
}