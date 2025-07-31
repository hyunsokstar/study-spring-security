package com.example.security.security_demo.user.service;

import com.example.security.security_demo.user.domain.User;
import com.example.security.security_demo.user.repository.UserRepository;
import com.example.security.security_demo.security.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil; // 👈 JWT 유틸리티 추가

    /**
     * 로그인 처리 - 성공 시 JWT 토큰과 사용자 정보 반환
     */
    public LoginResult login(String username, String password) {
        log.info("로그인 시도: {}", username);

        // 1. 사용자 존재 확인
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            log.warn("존재하지 않는 사용자: {}", username);
            return LoginResult.failure("존재하지 않는 사용자입니다.");
        }

        User user = userOpt.get();

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("비밀번호 불일치: {}", username);
            return LoginResult.failure("비밀번호가 틀렸습니다.");
        }

        // 3. JWT 토큰 생성 👈 핵심 추가!
        String token = jwtTokenUtil.generateToken(username);
        log.info("로그인 성공 및 토큰 생성: {}", username);

        return LoginResult.success(user, token);
    }

    /**
     * 토큰으로 사용자 조회 (인증된 요청용)
     */
    public Optional<User> findByToken(String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token);
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            log.error("토큰에서 사용자 조회 실패: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token, String username) {
        return jwtTokenUtil.validateToken(token, username);
    }

    /**
     * 사용자명으로 사용자 정보 조회 (기존 메서드 유지)
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 사용자 존재 여부만 확인 (빠른 체크용)
     */
    public boolean isUserExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * 로그인 결과를 담는 클래스 - JWT 토큰 포함
     */
    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final User user;
        private final String token; // 👈 JWT 토큰 추가

        private LoginResult(boolean success, String message, User user, String token) {
            this.success = success;
            this.message = message;
            this.user = user;
            this.token = token;
        }

        // 성공 결과 생성 (토큰 포함)
        public static LoginResult success(User user, String token) {
            return new LoginResult(true, "로그인 성공", user, token);
        }

        // 실패 결과 생성
        public static LoginResult failure(String message) {
            return new LoginResult(false, message, null, null);
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public User getUser() {
            return user;
        }

        public String getToken() { // 👈 토큰 getter 추가
            return token;
        }

        @Override
        public String toString() {
            return "LoginResult{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    ", user=" + (user != null ? user.getUsername() : "null") +
                    ", token=" + (token != null ? "***토큰있음***" : "null") +
                    '}';
        }
    }
}