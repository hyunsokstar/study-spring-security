package com.example.security.security_demo.user.controller;

import com.example.security.security_demo.user.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        try {
            userService.register(request.getUsername(), request.getPassword());
            return ResponseEntity.ok("회원가입 성공!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ 로그인 된 사용자 정보 확인용 (Optional)
    @GetMapping("/me")
    public ResponseEntity<String> me(@RequestParam String username) {
        return ResponseEntity.ok("안녕! " + username + " 님 🐰");
    }

    // ✅ 내부 DTO 클래스
    @Data
    public static class SignupRequest {
        private String username;
        private String password;
    }
}
