package com.example.security.security_demo.chatting.dto;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ChattingRoomResponse {
    private final UUID id;
    private final String name;
    private final Instant createdAt;
    private final Long messageCount;
    private final Instant lastMessageAt;

    private ChattingRoomResponse(
            UUID id,
            String name,
            Instant createdAt,
            Long messageCount,
            Instant lastMessageAt
    ) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.messageCount = messageCount;
        this.lastMessageAt = lastMessageAt;
    }

    public static ChattingRoomResponse of(
            UUID id,
            String name,
            Instant createdAt,
            Long messageCount,
            Instant lastMessageAt
    ) {
        return new ChattingRoomResponse(id, name, createdAt, messageCount, lastMessageAt);
    }
}