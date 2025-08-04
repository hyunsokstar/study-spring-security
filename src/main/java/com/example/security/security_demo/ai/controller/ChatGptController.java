package com.example.security.security_demo.ai.controller;

import com.example.security.security_demo.ai.dto.ChatRequest;
import com.example.security.security_demo.ai.dto.ChatResponse;
import com.example.security.security_demo.ai.service.ChatGptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class ChatGptController {

    private final ChatGptService chatGptService;

    // 🧪 간단한 GET 테스트
    @GetMapping("/hello")
    public ResponseEntity<ChatResponse> hello(@RequestParam(defaultValue = "안녕하세요!") String message) {
        ChatResponse response = chatGptService.chat(message);
        return ResponseEntity.ok(response);
    }

    // 🧪 POST 방식 테스트
    @PostMapping("/test")
    public ResponseEntity<ChatResponse> test(@RequestBody ChatRequest request) {
        ChatResponse response = chatGptService.chat(request.getMessage());
        return ResponseEntity.ok(response);
    }

    // 🎬 영화 추천 테스트
    @GetMapping("/movie")
    public ResponseEntity<ChatGptService.MovieInfo> movie(@RequestParam String actor) {
        ChatGptService.MovieInfo movieInfo = chatGptService.getMovieRecommendation(actor);
        return ResponseEntity.ok(movieInfo);
    }

    // 🧠 한국어 시스템 메시지로 대화
    @PostMapping("/chat-kr")
    public ResponseEntity<ChatResponse> chatKorean(@RequestBody ChatRequest request) {
        // 한국어 대화에 특화된 시스템 메시지 추가 가능
        ChatResponse response = chatGptService.chat(request.getMessage());
        return ResponseEntity.ok(response);
    }
}