package com.example.security.security_demo.user.controller;

import com.example.security.security_demo.user.dto.request.LoginRequest;
import com.example.security.security_demo.user.dto.request.SignupRequest;
import com.example.security.security_demo.user.dto.response.ApiResponse;
import com.example.security.security_demo.user.dto.response.LoginResponse;
import com.example.security.security_demo.user.service.LoginService;
import com.example.security.security_demo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final LoginService loginService;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@RequestBody SignupRequest request) {
        try {
            userService.register(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(ApiResponse.success("회원가입 성공!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("회원가입 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginService.LoginResult result = loginService.login(
                    request.getUsername(),
                    request.getPassword()
            );

            if (result.isSuccess()) {
                // 👇 토큰 포함해서 로그인 응답 생성
                LoginResponse loginResponse = LoginResponse.success(
                        result.getUser().getUsername(),
                        result.getToken() // 👈 토큰 추가!
                );

                return ResponseEntity.ok(
                        ApiResponse.success(
                                "로그인 성공",
                                loginResponse
                        )
                );
            } else {
                // 로그인 실패
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("❌ " + result.getMessage()));
            }

        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("❌ 로그인 처리 중 오류가 발생했습니다."));
        }
    }
    /**
     * 현재 사용자 정보
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> me(@RequestParam String username) {
        try {
            return loginService.findByUsername(username)
                    .map(user -> ResponseEntity.ok(
                            ApiResponse.success(
                                    "사용자 정보 조회 성공",
                                    "안녕! " + user.getUsername() + " 님 🐰"
                            )
                    ))
                    .orElse(ResponseEntity.badRequest()
                            .body(ApiResponse.error("사용자를 찾을 수 없습니다.")));
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 정보 조회 중 오류가 발생했습니다."));
        }
    }
}