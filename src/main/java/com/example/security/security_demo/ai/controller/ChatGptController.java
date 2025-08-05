package com.example.security.security_demo.ai.controller;

import com.example.security.security_demo.ai.dto.ChatRequest;
import com.example.security.security_demo.ai.dto.ChatResponse;
import com.example.security.security_demo.ai.service.ChatGptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // CORS 허용 (개발용)
public class ChatGptController {

    private final ChatGptService chatGptService;

    // 🧪 간단한 GET 테스트
    @GetMapping("/hello")
    public ResponseEntity<ChatResponse> hello(@RequestParam(defaultValue = "안녕하세요!") String message) {
        ChatResponse response = chatGptService.chat(message);
        return ResponseEntity.ok(response);
    }

    // 🧪 POST 방식 테스트 (일반 응답)
    @PostMapping("/test")
    public ResponseEntity<ChatResponse> test(@RequestBody ChatRequest request) {
        ChatResponse response = chatGptService.chat(request.getMessage());
        return ResponseEntity.ok(response);
    }

    // 🧠 한국어 시스템 메시지로 대화 (일반 응답)
    @PostMapping("/chat-kr")
    public ResponseEntity<ChatResponse> chatKorean(@RequestBody ChatRequest request) {
        ChatResponse response = chatGptService.chat(request.getMessage());
        return ResponseEntity.ok(response);
    }

    // 🔥 스트리밍 채팅 (GET 방식) - 테스트용
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String message) {
        return chatGptService.streamChat(message)
                .map(chunk -> "data: " + chunk + "\n\n"); // SSE 형식
    }

    // 🔥 스트리밍 채팅 (POST 방식) - 실제 사용 (기존 방식)
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChatPost(@RequestBody ChatRequest request) {
        return chatGptService.streamChat(request.getMessage());
        // Spring WebFlux가 자동으로 SSE 형식으로 변환
    }

    // 🔥 취소 가능한 스트리밍 채팅 (새로 추가)
    @PostMapping(value = "/stream/{streamId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChatWithId(
            @PathVariable String streamId,
            @RequestBody ChatRequest request) {
        return chatGptService.streamChatWithId(request.getMessage(), streamId);
    }

    // 🛑 스트리밍 취소 (새로 추가)
    @DeleteMapping("/stream/{streamId}")
    public ResponseEntity<Map<String, Object>> cancelStream(@PathVariable String streamId) {
        boolean cancelled = chatGptService.cancelStream(streamId);

        Map<String, Object> response = new HashMap<>();
        response.put("streamId", streamId);
        response.put("cancelled", cancelled);
        response.put("message", cancelled ? "Stream cancelled successfully" : "Stream not found or already completed");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    // 📊 활성 스트림 목록 조회 (디버깅용) (새로 추가)
    @GetMapping("/streams/active")
    public ResponseEntity<Map<String, Object>> getActiveStreams() {
        Set<String> activeStreams = chatGptService.getActiveStreams();

        Map<String, Object> response = new HashMap<>();
        response.put("activeStreams", activeStreams);
        response.put("count", activeStreams.size());
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    // 🎬 영화 추천 테스트 (구조화된 응답)
    @GetMapping("/movie")
    public ResponseEntity<ChatGptService.MovieInfo> movie(@RequestParam String actor) {
        ChatGptService.MovieInfo movieInfo = chatGptService.getMovieRecommendation(actor);
        return ResponseEntity.ok(movieInfo);
    }

    // 🌍 번역 기능
    @PostMapping("/translate")
    public ResponseEntity<ChatResponse> translate(
            @RequestParam String text,
            @RequestParam String targetLanguage) {
        ChatResponse response = chatGptService.translate(text, targetLanguage);
        return ResponseEntity.ok(response);
    }

    // 👨‍💻 코드 리뷰 기능
    @PostMapping("/review-code")
    public ResponseEntity<ChatResponse> reviewCode(@RequestBody String code) {
        ChatResponse response = chatGptService.reviewCode(code);
        return ResponseEntity.ok(response);
    }
}