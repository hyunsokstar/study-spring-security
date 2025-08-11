package com.example.security.security_demo.chatting.domain;

import com.example.security.security_demo.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@ToString(exclude = {"room", "user"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "chatting_room_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_room_user", columnNames = {"room_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_members_room", columnList = "room_id"),
                @Index(name = "idx_members_user", columnList = "user_id"),
                @Index(name = "idx_members_joined", columnList = "joined_at")
        })
public class ChattingRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_member_room"))
    private ChattingRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_member_user"))
    private User user;

    @Column(name = "nickname", length = 100)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", nullable = false, length = 20)
    private MemberRole memberRole = MemberRole.MEMBER;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_muted", nullable = false)
    private Boolean isMuted = false;

    @Column(name = "muted_until")
    private Instant mutedUntil;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

    @Column(name = "last_read_message_id", columnDefinition = "uuid")
    private UUID lastReadMessageId;

    @Column(name = "unread_count")
    private Integer unreadCount = 0;

    @Column(name = "notification_enabled", nullable = false)
    private Boolean notificationEnabled = true;

    @Builder
    public ChattingRoomMember(ChattingRoom room, User user, String nickname, MemberRole memberRole) {
        this.room = room;
        this.user = user;
        this.nickname = (nickname != null && !nickname.isBlank()) ? nickname : "anonymous";
        this.memberRole = (memberRole != null) ? memberRole : MemberRole.MEMBER;
        this.isActive = true;
        this.isMuted = false;
        this.notificationEnabled = true;
        this.unreadCount = 0;
    }

    public String getDisplayName() {
        return (nickname != null && !nickname.isBlank()) ? nickname : "anonymous";
    }

    public void updateLastRead(UUID messageId) {
        this.lastReadAt = Instant.now();
        this.lastReadMessageId = messageId;
        this.unreadCount = 0;
    }

    public void incrementUnreadCount() {
        this.unreadCount++;
    }

    public void mute(Instant until) {
        this.isMuted = true;
        this.mutedUntil = until;
    }

    public void unmute() {
        this.isMuted = false;
        this.mutedUntil = null;
    }

    public void leave() {
        this.isActive = false;
    }

    public void changeRole(MemberRole newRole) {
        this.memberRole = newRole;
    }

    public void changeNickname(String newNickname) {
        this.nickname = newNickname;
    }

    public boolean canSendMessage() {
        if (!isActive) {
            return false;
        }
        if (isMuted && mutedUntil != null && mutedUntil.isAfter(Instant.now())) {
            return false;
        }
        return true;
    }

    public boolean isOwner() {
        return memberRole == MemberRole.OWNER;
    }

    public boolean isAdmin() {
        return memberRole == MemberRole.OWNER || memberRole == MemberRole.ADMIN;
    }

    public boolean canManageRoom() {
        return memberRole == MemberRole.OWNER ||
                memberRole == MemberRole.ADMIN ||
                memberRole == MemberRole.MODERATOR;
    }

    public enum MemberRole {
        OWNER,       // 방장
        ADMIN,       // 관리자
        MODERATOR,   // 중재자
        MEMBER,      // 일반 멤버
        GUEST        // 게스트
    }
}