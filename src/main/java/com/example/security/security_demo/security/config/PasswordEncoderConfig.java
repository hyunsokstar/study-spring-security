// 📁 src/main/java/com/example/security/security_demo/security/config/PasswordEncoderConfig.java

package com.example.security.security_demo.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 🔧 PasswordEncoder 전용 설정 클래스
 *
 * 순환 의존성 해결을 위해 SecurityConfig에서 분리
 *
 * 순환 참조 문제:
 * JwtAuthenticationFilter → LoginService → PasswordEncoder (SecurityConfig) → JwtAuthenticationFilter
 *
 * 해결 방법:
 * PasswordEncoder를 별도 설정 클래스로 분리하여 순환 참조 차단
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}