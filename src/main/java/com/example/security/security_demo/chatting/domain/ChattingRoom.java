package com.example.security.security_demo.chatting.domain;

import com.example.security.security_demo.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@ToString(exclude = {"messages", "members", "creator"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "chatting_rooms", indexes = {
        @Index(name = "idx_chatting_rooms_name", columnList = "name"),
        @Index(name = "idx_chatting_rooms_created", columnList = "created_at"),
        @Index(name = "idx_chatting_rooms_type", columnList = "room_type")
})
public class ChattingRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private RoomType roomType = RoomType.PUBLIC;

    @Column(name = "max_members")
    private Integer maxMembers = 100;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by",
            foreignKey = @ForeignKey(name = "fk_room_creator"))
    private User creator;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<ChattingMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChattingRoomMember> members = new ArrayList<>();

    @Version
    private Long version;

    @Builder
    public ChattingRoom(String name, String description, RoomType roomType,
                        Integer maxMembers, User creator) {
        this.name = name;
        this.description = description;
        this.roomType = (roomType != null) ? roomType : RoomType.PUBLIC;
        this.maxMembers = (maxMembers != null) ? maxMembers : 100;
        this.creator = creator;
        this.isActive = true;
    }

    public void updateLastMessageAt() {
        this.lastMessageAt = Instant.now();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public void addMember(ChattingRoomMember member) {
        this.members.add(member);
        member.setRoom(this);
    }

    public void removeMember(ChattingRoomMember member) {
        this.members.remove(member);
        member.setRoom(null);
    }

    public boolean hasMember(User user) {
        return members.stream()
                .anyMatch(member -> member.getUser().equals(user) && member.getIsActive());
    }

    public long getActiveMemberCount() {
        return members.stream()
                .filter(ChattingRoomMember::getIsActive)
                .count();
    }

    public enum RoomType {
        PUBLIC,      // 공개 채팅방
        PRIVATE,     // 비공개 채팅방
        DIRECT,      // 1:1 다이렉트 메시지
        GROUP        // 그룹 채팅방
    }
}