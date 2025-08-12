package com.example.security.security_demo.chatting.presentation.dto;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ChattingRoomResponse {
    private final UUID id;
    private final String name;
    private final CreatorInfo creator;
    private final Instant createdAt;
    private final Long messageCount;
    private final Instant lastMessageAt;

    private ChattingRoomResponse(UUID id, String name, CreatorInfo creator, Instant createdAt, Long messageCount, Instant lastMessageAt) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.createdAt = createdAt;
        this.messageCount = messageCount;
        this.lastMessageAt = lastMessageAt;
    }

    public static ChattingRoomResponse of(UUID id, String name, CreatorInfo creator, Instant createdAt, Long messageCount, Instant lastMessageAt) {
        return new ChattingRoomResponse(id, name, creator, createdAt, messageCount, lastMessageAt);
    }
    
    // 하위 호환성을 위한 메서드 (creatorName 문자열로 받는 경우)
    public static ChattingRoomResponse of(UUID id, String name, String creatorName, Instant createdAt, Long messageCount, Instant lastMessageAt) {
        CreatorInfo creator = new CreatorInfo(null, creatorName);
        return new ChattingRoomResponse(id, name, creator, createdAt, messageCount, lastMessageAt);
    }
    
    @Getter
    public static class CreatorInfo {
        private final Long id;
        private final String username;
        
        public CreatorInfo(Long id, String username) {
            this.id = id;
            this.username = username;
        }
    }
}