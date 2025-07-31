// 📁 src/main/java/com/example/security/security_demo/user/controller/UserController.java

package com.example.security.security_demo.user.controller;

import com.example.security.security_demo.user.domain.User;
import com.example.security.security_demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 🔒 JWT 토큰 필수 - 현재 로그인한 사용자 정보 조회
     *
     * 요청 방법:
     * GET /api/users/me
     * Authorization: Bearer {JWT토큰}
     *
     * 동작 과정:
     * 1. JwtAuthenticationFilter에서 토큰 검증
     * 2. 유효한 토큰이면 SecurityContext에 인증 정보 설정
     * 3. 이 메서드에서 SecurityContext에서 사용자 정보 추출
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        log.info("📥 현재 사용자 정보 요청");

        // 🎯 JWT 필터에서 설정한 SecurityContext에서 인증 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName(); // JWT 토큰에서 추출한 사용자명
            log.info("✅ 인증된 사용자: {}", username);

            // DB에서 사용자 정보 조회
            return userService.findByUsername(username)
                    .map(user -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("message", "현재 사용자 정보");
                        response.put("data", Map.of(
                                "id", user.getId(),
                                "username", user.getUsername(),
                                "roles", user.getUserRoles().stream()
                                        .map(ur -> ur.getRole().getName())
                                        .toList()
                        ));
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "사용자를 찾을 수 없습니다"
                    )));
        }

        // 이 상황은 JWT 필터를 거쳤는데도 인증 정보가 없는 경우 (거의 발생하지 않음)
        return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "인증되지 않은 사용자입니다"
        ));
    }

    /**
     * 🔒 JWT 토큰 필수 - 모든 사용자 목록 조회
     *
     * 요청 방법:
     * GET /api/users/list
     * Authorization: Bearer {JWT토큰}
     *
     * 토큰이 없으면 401 응답, 토큰이 있으면 전체 사용자 목록 반환
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        log.info("📥 전체 사용자 목록 요청");

        List<User> users = userService.findAll();

        // 🔒 비밀번호는 응답에서 제외
        List<Map<String, Object>> userList = users.stream()
                .map(user -> Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "roles", user.getUserRoles().stream()
                                .map(ur -> ur.getRole().getName())
                                .toList()
                ))
                .toList();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "전체 사용자 목록");
        response.put("data", userList);
        response.put("count", userList.size());

        return ResponseEntity.ok(response);
    }

    /**
     * 🔐 JWT 토큰 + ADMIN 권한 필수 - 사용자 삭제
     *
     * 요청 방법:
     * DELETE /api/users/{userId}
     * Authorization: Bearer {JWT토큰}
     *
     * 조건:
     * 1. 유효한 JWT 토큰 필요
     * 2. 토큰의 사용자가 ADMIN 권한 보유 필요
     *
     * @PreAuthorize가 SecurityContext의 권한 정보를 확인하여 접근 제어
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')") // 👈 ADMIN 권한 체크 (메서드 레벨 보안)
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long userId) {
        log.info("📥 사용자 삭제 요청: userId={}", userId);

        // TODO: 실제 삭제 로직 구현
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "사용자 삭제 완료 (ADMIN 권한 확인됨)");
        response.put("deletedUserId", userId);

        return ResponseEntity.ok(response);
    }

    /**
     * 🌐 공개 엔드포인트 - 사용자 수 조회 (JWT 토큰 불필요)
     *
     * 요청 방법:
     * GET /api/users/count
     * (Authorization 헤더 없어도 됨)
     *
     * SecurityConfig에서 .permitAll()로 설정되어 있어서
     * JWT 필터를 거치지만 인증 체크를 하지 않음
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getUserCount() {
        log.info("📥 사용자 수 조회 (공개)");

        long count = userService.findAll().size();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "전체 사용자 수");
        response.put("count", count);
        response.put("note", "이 엔드포인트는 JWT 토큰 불필요 (공개)");

        return ResponseEntity.ok(response);
    }

    /**
     * 🔒 JWT 토큰 필수 - 토큰 정보 확인 (디버깅용)
     *
     * 요청 방법:
     * GET /api/users/token-info
     * Authorization: Bearer {JWT토큰}
     *
     * SecurityContext에 설정된 인증 정보를 그대로 반환
     * JWT 필터가 제대로 동작하는지 확인하는 용도
     */
    @GetMapping("/token-info")
    public ResponseEntity<Map<String, Object>> getTokenInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> tokenInfo = new HashMap<>();
        tokenInfo.put("authenticated", auth.isAuthenticated());
        tokenInfo.put("principal", auth.getName());              // JWT에서 추출한 사용자명
        tokenInfo.put("authorities", auth.getAuthorities());     // 권한 목록
        tokenInfo.put("details", auth.getDetails());             // 요청 세부 정보 (IP 등)

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "JWT 토큰 정보");
        response.put("data", tokenInfo);

        return ResponseEntity.ok(response);
    }
}

/*
📋 엔드포인트별 JWT 토큰 요구사항:

🌐 공개 엔드포인트 (토큰 불필요):
- GET /api/users/count         ✅ 누구나 접근 가능

🔒 인증 필요 엔드포인트 (JWT 토큰 필수):
- GET /api/users/me            ✅ 유효한 토큰 있으면 접근 가능
- GET /api/users/list          ✅ 유효한 토큰 있으면 접근 가능
- GET /api/users/token-info    ✅ 유효한 토큰 있으면 접근 가능

🔐 권한 필요 엔드포인트 (JWT 토큰 + 특정 권한 필수):
- DELETE /api/users/{userId}   ✅ 토큰 + ADMIN 권한 필요

🎯 토큰 없이 보호된 엔드포인트 호출 시:
1. JwtAuthenticationFilter에서 토큰 없음 감지
2. SecurityContext 비어있음
3. AuthorizationFilter에서 인증 필요 판단
4. JwtAuthenticationEntryPoint에서 401 JSON 응답

🎯 토큰 있이 보호된 엔드포인트 호출 시:
1. JwtAuthenticationFilter에서 토큰 검증
2. 유효하면 SecurityContext에 인증 정보 설정
3. AuthorizationFilter에서 인증됨 확인
4. Controller 메서드 실행
*/