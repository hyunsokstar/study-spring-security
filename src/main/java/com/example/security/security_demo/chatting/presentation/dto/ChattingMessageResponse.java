package com.example.security.security_demo.chatting.presentation.dto;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ChattingMessageResponse {
    private final UUID id;
    private final UUID roomId;
    private final String sender;
    private final String content;
    private final Instant createdAt;

    public ChattingMessageResponse(UUID id, UUID roomId, String sender, String content, Instant createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.sender = sender;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static ChattingMessageResponse of(UUID id, UUID roomId, String sender, String content, Instant createdAt) {
        return new ChattingMessageResponse(id, roomId, sender, content, createdAt);
    }
}