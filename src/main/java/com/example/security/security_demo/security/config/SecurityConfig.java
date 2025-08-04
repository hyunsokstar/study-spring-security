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

/*
🔄 JWT + CORS 인증 플로우:

📥 클라이언트 요청 (Tauri 앱에서)
    ↓
🌐 CORS 필터 (Spring Security 내장)
    ├─ Origin 헤더 확인
    ├─ 허용된 도메인인지 검증
    ├─ Preflight 요청 처리 (OPTIONS)
    └─ CORS 헤더 추가
    ↓
🔍 JwtAuthenticationFilter (우리가 추가한 필터)
    ├─ Authorization 헤더에서 토큰 추출
    ├─ 토큰 유효성 검증 (JwtTokenUtil)
    ├─ 유효하면 SecurityContext에 인증 정보 설정
    └─ 유효하지 않으면 SecurityContext 비워둠
    ↓
🛡️ AuthorizationFilter (Spring Security 기본)
    ├─ SecurityContext 확인
    ├─ 인증 필요한 경로면 인증 상태 체크
    ├─ 인증됨 → Controller 호출 허용
    └─ 미인증 → AuthenticationEntryPoint 호출
    ↓
📤 응답 (CORS 헤더 포함)
    ├─ 성공: Controller 응답 + CORS 헤더
    └─ 실패: 401 JSON 에러 응답 + CORS 헤더

🎯 이제 Tauri 앱에서 백엔드 API 호출이 CORS 에러 없이 가능!
*/