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
                LoginResponse loginResponse = LoginResponse.success(
                        result.getUser().getUsername(),
                        result.getToken()
                );

                return ResponseEntity.ok(
                        ApiResponse.success("로그인 성공", loginResponse)
                );
            } else {
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
     * 현재 사용자 정보 - 간소화됨 ✅
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> me(@RequestHeader("Authorization") String authHeader) {
        LoginService.AuthResult result = loginService.getCurrentUser(authHeader);

        if (result.isSuccess()) {
            return ResponseEntity.ok(
                    ApiResponse.success("사용자 정보 조회 성공", result.getData())
            );
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("❌ " + result.getMessage()));
        }
    }

    /**
     * 토큰 유효성 검증 - 간소화됨 ✅
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        LoginService.AuthResult result = loginService.validateTokenFromHeader(authHeader);

        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success("✅ " + result.getMessage()));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("❌ " + result.getMessage()));
        }
    }
}