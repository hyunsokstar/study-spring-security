package com.example.security.security_demo.chat.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_chat_messages_room_id_created_at", columnList = "room_id, created_at")
})
public class Message {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "sender_user_id", nullable = false)
    private String senderUserId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    private Message(UUID id, UUID roomId, String senderUserId, String content, Instant createdAt) {
        this.id = id;
        this.roomId = roomId;
        this.senderUserId = senderUserId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static Message of(UUID roomId, String senderUserId, String content) {
        return new Message(UUID.randomUUID(), roomId, senderUserId, content, Instant.now());
    }
}
