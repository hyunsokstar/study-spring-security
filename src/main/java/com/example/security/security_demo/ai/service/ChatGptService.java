package com.example.security.security_demo.ai.service;

import com.example.security.security_demo.ai.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatGptService {

    private final ChatClient chatClient;

    public ChatGptService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("당신은 한국어로 친절하게 답변하는 AI 어시스턴트입니다. 정확하고 도움이 되는 정보를 제공해주세요.")
                .build();
    }

    public ChatResponse chat(String message) {
        String response = this.chatClient.prompt()
                .user(message)
                .call()
                .content();

        return new ChatResponse(response);
    }

    // 구조화된 응답 예제
    public record MovieInfo(String actor, java.util.List<String> movies) {}

    public MovieInfo getMovieRecommendation(String actor) {
        return this.chatClient.prompt()
                .user("다음 배우의 주요 영화 목록을 JSON 형태로 생성해주세요: " + actor + ". 최대 5편까지만 포함해주세요.")
                .call()
                .entity(MovieInfo.class);
    }

    // 추가 메서드: 번역 기능
    public ChatResponse translate(String text, String targetLanguage) {
        String response = this.chatClient.prompt()
                .user("다음 텍스트를 " + targetLanguage + "로 번역해주세요: " + text)
                .call()
                .content();

        return new ChatResponse(response);
    }

    // 추가 메서드: 코드 리뷰
    public ChatResponse reviewCode(String code) {
        String response = this.chatClient.prompt()
                .user("다음 코드를 리뷰하고 개선점을 알려주세요:\n\n" + code)
                .call()
                .content();

        return new ChatResponse(response);
    }
}