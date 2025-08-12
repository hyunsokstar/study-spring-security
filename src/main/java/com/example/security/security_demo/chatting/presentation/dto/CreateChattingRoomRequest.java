package com.example.security.security_demo.chatting.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateChattingRoomRequest {
    @NotBlank
    @Size(max = 100)
    private String name;
    
    // Lombok이 제대로 작동하지 않을 때를 대비한 수동 getter
    public String getName() {
        return name;
    }
}