package com.example.security.security_demo.chatting.dto;

import com.example.security.security_demo.chatting.domain.ChattingRoom.RoomType;
import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateChattingRoomRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    // PUBLIC / PRIVATE / DIRECT / GROUP
    private RoomType roomType = RoomType.PUBLIC;

    @Min(2) @Max(500)
    private Integer maxMembers = 100;

    // 비공개/그룹 확장용 (선택)
    private String inviteCode;
}