package com.example.security.security_demo.user.service;

import com.example.security.security_demo.user.domain.Role;
import com.example.security.security_demo.user.domain.User;
import com.example.security.security_demo.user.domain.UserRole;
import com.example.security.security_demo.user.repository.RoleRepository;
import com.example.security.security_demo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 처리
     */
    public User register(String username, String rawPassword) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name("ROLE_USER").build()
                ));

        // 🔥 중간 엔티티 생성
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .build();

        UserRole userRoleEntity = UserRole.builder()
                .user(user)
                .role(userRole)
                .build();

        // 👇 연관관계 세팅
        user.setUserRoles(List.of(userRoleEntity));

        return userRepository.save(user);
    }

    /**
     * 유저 조회 (로그인/검증용)
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 모든 유저 조회 (관리자 용도 등)
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }
}
