// src/main/java/com/example/security/security_demo/user/dto/request/LoginRequest.java
package com.example.security.security_demo.user.dto.request;

import lombok.Data;

/**
 * 로그인 요청 DTO
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}