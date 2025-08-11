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
@Table(name = "chatting_rooms")
public class ChattingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(length = 100, nullable = false)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // 기존 DB에 NOT NULL 제약이 있는 컬럼들 매핑
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private RoomType roomType = RoomType.PUBLIC;

    public ChattingRoom(String name) {
        this.name = name;
        this.isActive = true;
        this.roomType = RoomType.PUBLIC; // 안전하게 기본값 지정
    }

    public enum RoomType {
        PUBLIC, PRIVATE, DIRECT, GROUP
    }
}