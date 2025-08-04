// 📁 src/main/java/com/example/security/security_demo/config/CorsConfig.java

package com.example.security.security_demo.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🌐 Tauri 앱에서의 요청 허용
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "tauri://localhost",     // Tauri 기본 스키마
                "http://localhost:*",    // 개발 서버 (포트 번호 상관없이)
                "https://localhost:*",   // HTTPS 개발 서버
                "http://127.0.0.1:*",    // 로컬호스트 IP
                "https://127.0.0.1:*"    // HTTPS 로컬호스트 IP
        ));

        // 📝 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // 🔑 허용할 헤더 (JWT 토큰 포함)
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",     // JWT 토큰
                "Content-Type",      // JSON 요청
                "Accept",           // 응답 타입
                "Origin",           // 요청 출처
                "X-Requested-With"  // AJAX 요청
        ));

        // 🍪 쿠키 및 인증 정보 전송 허용
        configuration.setAllowCredentials(true);

        // ⏰ Preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        // 📍 모든 경로에 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}