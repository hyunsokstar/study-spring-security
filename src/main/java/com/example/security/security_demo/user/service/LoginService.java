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
    private final JwtTokenUtil jwtTokenUtil;

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

        // 3. JWT 토큰 생성
        String token = jwtTokenUtil.generateToken(username);
        log.info("로그인 성공 및 토큰 생성: {}", username);

        return LoginResult.success(user, token);
    }

    /**
     * Authorization 헤더에서 사용자 정보 조회 ✅ 새로 추가
     */
    public AuthResult getCurrentUser(String authHeader) {
        try {
            // 1. Authorization 헤더 검증
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return AuthResult.failure("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
            }

            // 2. 토큰 추출
            String token = authHeader.substring(7); // "Bearer " 제거

            // 3. 토큰으로 사용자 조회
            Optional<User> userOpt = findByToken(token);
            if (userOpt.isEmpty()) {
                return AuthResult.failure("유효하지 않은 토큰입니다.");
            }

            // 4. 사용자 정보 변환
            User user = userOpt.get();
            UserInfoResponse userInfo = createUserInfoResponse(user);

            return AuthResult.success(userInfo);

        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생", e);
            return AuthResult.failure("사용자 정보 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 토큰 유효성 검증 ✅ 새로 추가
     */
    public AuthResult validateTokenFromHeader(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return AuthResult.failure("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
            }

            String token = authHeader.substring(7);
            boolean isValid = findByToken(token).isPresent();

            if (isValid) {
                return AuthResult.success("유효한 토큰입니다.");
            } else {
                return AuthResult.failure("유효하지 않은 토큰입니다.");
            }

        } catch (Exception e) {
            log.error("토큰 검증 중 오류 발생", e);
            return AuthResult.failure("토큰 검증 중 오류가 발생했습니다.");
        }
    }

    /**
     * 사용자 정보 응답 객체 생성 (private 헬퍼 메서드)
     */
    private UserInfoResponse createUserInfoResponse(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getUsername()) // 실제로는 User 엔티티에 name 필드 추가 필요
                .department("고객상담팀") // 추후 User 엔티티나 별도 테이블에서 가져와야 함
                .role("상담원") // 추후 Role 엔티티와 연관관계 설정 필요
                .build();
    }

    /**
     * 토큰으로 사용자 조회 (기존 메서드)
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
     * 토큰 유효성 검증 (기존 메서드)
     */
    public boolean validateToken(String token, String username) {
        return jwtTokenUtil.validateToken(token, username);
    }

    /**
     * 사용자명으로 사용자 정보 조회 (기존 메서드)
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 사용자 존재 여부만 확인 (기존 메서드)
     */
    public boolean isUserExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    // ============== 결과 클래스들 ==============

    /**
     * 로그인 결과 클래스
     */
    public static class LoginResult {
        private final boolean success;
        private final String message;
        private final User user;
        private final String token;

        private LoginResult(boolean success, String message, User user, String token) {
            this.success = success;
            this.message = message;
            this.user = user;
            this.token = token;
        }

        public static LoginResult success(User user, String token) {
            return new LoginResult(true, "로그인 성공", user, token);
        }

        public static LoginResult failure(String message) {
            return new LoginResult(false, message, null, null);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
        public String getToken() { return token; }
    }

    /**
     * 인증 결과 클래스 ✅ 새로 추가
     */
    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final Object data;

        private AuthResult(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public static AuthResult success(Object data) {
            return new AuthResult(true, "성공", data);
        }

        public static AuthResult success(String message) {
            return new AuthResult(true, message, null);
        }

        public static AuthResult failure(String message) {
            return new AuthResult(false, message, null);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
    }

    /**
     * 사용자 정보 응답 DTO ✅ 새로 추가
     */
    @lombok.Builder
    @lombok.Getter
    public static class UserInfoResponse {
        private Long id;
        private String username;
        private String name;
        private String department;
        private String role;
    }
}