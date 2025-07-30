// src/main/java/com/example/security/security_demo/user/dto/request/SignupRequest.java
package com.example.security.security_demo.user.dto.request;

import lombok.Data;

/**
 * 회원가입 요청 DTO
 */
@Data
public class SignupRequest {
    private String username;
    private String password;
}