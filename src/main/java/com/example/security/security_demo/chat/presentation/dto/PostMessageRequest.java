package com.example.security.security_demo.chat.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostMessageRequest {
    @NotBlank
    private String senderUserId;

    @NotBlank
    private String content;
}
