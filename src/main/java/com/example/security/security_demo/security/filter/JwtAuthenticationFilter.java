// 📁 src/main/java/com/example/security/security_demo/security/filter/JwtAuthenticationFilter.java

package com.example.security.security_demo.security.filter;

import com.example.security.security_demo.security.util.JwtTokenUtil;
import com.example.security.security_demo.user.service.LoginService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final LoginService loginService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // 🔍 Step 1: Authorization 헤더에서 JWT 토큰 추출
            String token = extractTokenFromRequest(request);

            if (token != null) {
                log.debug("📥 JWT 토큰 발견: {}...", token.substring(0, Math.min(token.length(), 20)));

                // 🔍 Step 2: 토큰에서 사용자명 추출
                String username = jwtTokenUtil.getUsernameFromToken(token);

                // 🔍 Step 3: 현재 SecurityContext에 인증 정보가 없는 경우에만 처리
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // 🔍 Step 4: 토큰 유효성 검증
                    if (jwtTokenUtil.validateToken(token, username)) {

                        // 🔍 Step 5: 사용자 존재 여부 확인
                        var userOpt = loginService.findByUsername(username);

                        if (userOpt.isPresent()) {
                            // 🎯 Step 6: Spring Security 인증 객체 생성
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(
                                            username,                                    // Principal
                                            null,                                       // Credentials
                                            List.of(new SimpleGrantedAuthority("ROLE_USER")) // Authorities
                                    );

                            // 🔍 Step 7: 요청 세부정보 설정
                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            // 🎯 Step 8: SecurityContextHolder에 인증 정보 설정
                            SecurityContextHolder.getContext().setAuthentication(authToken);

                            log.debug("✅ JWT 인증 성공: {} (IP: {})", username, request.getRemoteAddr());
                        } else {
                            log.warn("❌ 토큰은 유효하지만 사용자를 찾을 수 없음: {}", username);
                        }
                    } else {
                        log.warn("❌ 유효하지 않은 JWT 토큰: {}", username);
                    }
                }
            } else {
                log.debug("🔍 JWT 토큰 없음 - 공개 엔드포인트이거나 인증 불필요");
            }

        } catch (Exception e) {
            log.error("💥 JWT 인증 처리 중 오류 발생", e);
            // SecurityContext는 비어있는 상태로 유지됨 (인증 실패)
        }

        // 🔄 Step 9: 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청에서 JWT 토큰 추출
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIs... 형태에서 토큰만 추출
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 제거
        }

        return null;
    }

    /**
     * 특정 경로는 JWT 필터 적용 제외 (성능 최적화)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 인증이 필요 없는 경로들은 JWT 검증 스킵
        return path.startsWith("/auth/login") ||
                path.startsWith("/auth/signup") ||
                path.startsWith("/h2-console") ||
                path.startsWith("/favicon.ico") ||
                path.startsWith("/error");
    }
}