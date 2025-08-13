// 📁 src/main/java/com/example/security/security_demo/security/config/CorsConfig.java

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

        // 🌐 허용할 Origin 설정 (Tauri + 개발 서버 + 배포 환경)
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:1420",       // ⭐ Vite 개발 서버 (현재 에러 origin)
                "http://localhost:3000",       // React 개발 서버
                "http://localhost:5173",       // Vite 기본 포트
                "http://tauri.localhost",      // Tauri 앱
                "https://tauri.localhost",
                "tauri://localhost",
                "tauri://*",
                // 배포 환경 IP 주소들
                "http://3.36.184.159:3000",    // 프론트엔드 배포 서버
                "http://43.200.234.52:8080",   // 백엔드 배포 서버
                "https://3.36.184.159:3000",   // HTTPS 프론트엔드
                "https://43.200.234.52:8080"   // HTTPS 백엔드
        ));

        // 📝 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // 📋 모든 헤더 허용 (개발 환경)
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 🍪 인증 정보 포함 허용 (쿠키, Authorization 헤더 등)
        configuration.setAllowCredentials(true);

        // 📤 노출할 헤더 (클라이언트에서 접근 가능한 헤더)
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition"
        ));

        // ⏱️ preflight 요청 캐시 시간 (1시간)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // 모든 경로에 적용

        return source;
    }
}