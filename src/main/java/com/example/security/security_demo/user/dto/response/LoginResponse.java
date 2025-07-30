// src/main/java/com/example/security/security_demo/user/dto/response/LoginResponse.java
package com.example.security.security_demo.user.dto.response;

import lombok.Data;

/**
 * 로그인 응답 DTO - JWT 토큰 포함
 */
@Data
public class LoginResponse {
    private String username;
    private String message;
    private String token;      // 👈 JWT 토큰
    private String tokenType;  // 👈 토큰 타입

    public LoginResponse(String username, String message, String token) {
        this.username = username;
        this.message = message;
        this.token = token;
        this.tokenType = "Bearer"; // JWT는 보통 Bearer 타입
    }

    // 로그인 성공 응답 생성 (JWT 토큰 포함) - 파라미터로 토큰 받기
    public static LoginResponse success(String username, String token) {
        return new LoginResponse(
                username,
                "🎉 로그인 성공! 환영합니다 " + username + "님!",
                token // 👈 파라미터로 받은 토큰 사용
        );
    }
}