package com.example.security.security_demo.chatting.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendChattingMessageRequest {

    @NotBlank
    @Size(max = 100)
    private String sender;   // 간단히 문자열로 처리(인증 연동 시 토큰에서 추출로 대체 가능)

    @NotBlank
    @Size(max = 2000)
    private String content;
}