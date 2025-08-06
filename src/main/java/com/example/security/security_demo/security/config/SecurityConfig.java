// 📁 src/main/java/com/example/security/security_demo/security/config/SecurityConfig.java

package com.example.security.security_demo.security.config;

import com.example.security.security_demo.security.filter.JwtAuthenticationFilter;
import com.example.security.security_demo.security.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // 👈 @PreAuthorize, @PostAuthorize 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    // 🔧 JWT 관련 컴포넌트 주입
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;  // 🌐 CORS 설정 주입

    // 🚫 기본 UserDetailsService 비활성화 (기본 패스워드 생성 방지)
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("JWT 기반 인증만 사용합니다.");
        };
    }

    // 🔐 PasswordEncoder는 PasswordEncoderConfig에서 정의됨 (중복 제거)

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 🌐 CORS 설정 활성화 (CorsConfig에서 정의한 설정 사용)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // 🔐 URL별 접근 권한 설정
                .authorizeHttpRequests(authorize -> authorize

                        // 🌐 인증 없이 접근 가능한 엔드포인트 (PUBLIC)
                        .requestMatchers(HttpMethod.POST, "/auth/signup").permitAll()    // 회원가입
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()     // 로그인
                        .requestMatchers(HttpMethod.GET, "/api/users/count").permitAll() // 사용자 수 조회 (공개)

                        // 🤖 AI 테스트 엔드포인트 (인증 없이 접근 가능)
                        .requestMatchers(HttpMethod.GET, "/api/ai/hello").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ai/test").permitAll()
                        .requestMatchers("/api/ai/**").permitAll()  // 👈 모든 HTTP 메서드 허용

                        .requestMatchers("/error").permitAll()                          // 에러 페이지
                        .requestMatchers("/favicon.ico").permitAll()                    // 파비콘

                        // 🔒 나머지 모든 요청은 JWT 토큰 인증 필수 (AUTHENTICATED)
                        // ⭐ 여기가 핵심! 토큰 없으면 401 응답, 토큰 있으면 SecurityContext 설정
                        .anyRequest().authenticated()
                )

                // 🚫 CSRF 비활성화 (JWT는 stateless이므로 CSRF 공격 불가능)
                .csrf(csrf -> csrf.disable())

                // 📝 세션 정책: STATELESS
                // - 서버에서 세션 관리 안 함
                // - 매 요청마다 JWT 토큰으로 인증 상태 판단
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 🎯 핵심! JWT 필터를 Spring Security 필터 체인에 추가
                // - UsernamePasswordAuthenticationFilter 앞에 배치
                // - 모든 요청에서 JWT 토큰 검증 → SecurityContext 설정
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 🚨 인증 실패 시 커스텀 JSON 응답 처리
                // - 기본 HTML 에러 페이지 대신 JSON 응답
                // - 401 Unauthorized + 상세한 에러 메시지
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint));

        return http.build();
    }
}