package com.example.security.security_demo.chatting.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendChattingMessageRequest {

    // 인증 연동 시 senderId는 토큰에서 추출하고 본 요청 필드 제거 가능
    @NotBlank
    private String senderId;

    @NotBlank
    private String content;
}