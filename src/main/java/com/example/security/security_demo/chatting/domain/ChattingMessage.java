package com.example.security.security_demo.chatting.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "chatting_messages", indexes = {
        @Index(name = "idx_chatting_messages_room_created_at", columnList = "room_id, created_at")
})
public class ChattingMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_message_room"))
    private ChattingRoom room;

    @Column(name = "sender", length = 100, nullable = false)
    private String sender;

    @Column(name = "content", length = 2000, nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    private ChattingMessage(ChattingRoom room, String sender, String content) {
        this.room = room;
        this.sender = sender;
        this.content = content;
    }
}