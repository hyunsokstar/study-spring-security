// src/main/java/com/example/security/security_demo/user/dto/response/ApiResponse.java
package com.example.security.security_demo.user.dto.response;

import lombok.Data;

/**
 * 공통 API 응답 형식
 */
@Data
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;

    private ApiResponse(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static ApiResponse success(String message) {
        return new ApiResponse(true, message, null);
    }

    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, data);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(false, message, null);
    }
}