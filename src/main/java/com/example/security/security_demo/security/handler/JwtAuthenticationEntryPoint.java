package com.example.security.security_demo.security.handler;// 📁 src/main/java/com/example/security/security_demo/config/JwtAuthenticationEntryPoint.java


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.warn("인증 실패 - IP: {}, URI: {}, 에러: {}",
                request.getRemoteAddr(),
                request.getRequestURI(),
                authException.getMessage());

        // 🎯 JSON 응답 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        // 📝 에러 응답 데이터 구성
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "AUTHENTICATION_FAILED");
        errorResponse.put("message", determineErrorMessage(request, authException));
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("path", request.getRequestURI());
        errorResponse.put("status", 401);

        // 🔍 디버그 정보 (개발환경에서만 포함)
        if (isDebugMode()) {
            errorResponse.put("details", authException.getMessage());
            errorResponse.put("type", authException.getClass().getSimpleName());
        }

        // JSON 응답 전송
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        response.getWriter().flush();
    }

    /**
     * 상황에 맞는 에러 메시지 결정
     */
    private String determineErrorMessage(HttpServletRequest request, AuthenticationException authException) {
        String authHeader = request.getHeader("Authorization");

        // Authorization 헤더가 없는 경우
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return "인증 토큰이 필요합니다. Authorization 헤더에 Bearer 토큰을 포함해주세요.";
        }

        // Bearer 형식이 아닌 경우
        if (!authHeader.startsWith("Bearer ")) {
            return "올바르지 않은 토큰 형식입니다. 'Bearer {토큰}' 형식으로 전송해주세요.";
        }

        // 토큰이 있지만 유효하지 않은 경우
        return "유효하지 않거나 만료된 토큰입니다. 다시 로그인해주세요.";
    }

    /**
     * 디버그 모드 여부 확인 (실제 환경에서는 프로퍼티로 관리)
     */
    private boolean isDebugMode() {
        // TODO: application.yml에서 설정값으로 관리
        return true; // 개발환경에서는 true, 운영환경에서는 false
    }
}