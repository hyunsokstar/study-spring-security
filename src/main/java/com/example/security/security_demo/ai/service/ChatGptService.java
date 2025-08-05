package com.example.security.security_demo.ai.service;

import com.example.security.security_demo.ai.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Service
public class ChatGptService {

    private final ChatClient chatClient;

    public ChatGptService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                당신은 한국어로 친절하게 답변하는 AI 어시스턴트입니다. 
                정확하고 도움이 되는 정보를 제공해주세요.
                
                답변 시 다음 형식을 따라주세요:
                - 문단과 문단 사이는 빈 줄로 구분
                - 긴 설명은 여러 문단으로 나누어 가독성 향상
                - 목록이 필요한 경우 번호나 불릿 포인트 사용
                - 코드는 백틱(```)으로 감싸서 표시
                """)
                .build();
    }

    // 💬 일반 채팅 (전체 응답 한 번에)
    public ChatResponse chat(String message) {
        String response = this.chatClient.prompt()
                .user(message)
                .call()
                .content();

        return new ChatResponse(response);
    }

    // 🔥 스트리밍 채팅 (실시간 타이핑 효과)
    public Flux<String> streamChat(String message) {
        return this.chatClient.prompt()
                .user(message)
                .stream()
                .content()
                // 100ms 마다, 또는 최대 10개 토큰이 모일 때마다 한 번에 던져준다
                .bufferTimeout(10, Duration.ofMillis(100))
                // 리스트를 문자열로 합쳐서 내려준다
                .map(tokens -> String.join("", tokens));
    }

    // 🎬 구조화된 응답 예제 (영화 정보)
    public record MovieInfo(String actor, java.util.List<String> movies) {}

    public MovieInfo getMovieRecommendation(String actor) {
        return this.chatClient.prompt()
                .user("다음 배우의 주요 영화 목록을 JSON 형태로 생성해주세요: " + actor + ". 최대 5편까지만 포함해주세요.")
                .call()
                .entity(MovieInfo.class);
    }

    // 🌍 번역 기능
    public ChatResponse translate(String text, String targetLanguage) {
        String response = this.chatClient.prompt()
                .user("다음 텍스트를 " + targetLanguage + "로 번역해주세요: " + text)
                .call()
                .content();

        return new ChatResponse(response);
    }

    // 👨‍💻 코드 리뷰 기능
    public ChatResponse reviewCode(String code) {
        String response = this.chatClient.prompt()
                .user("다음 코드를 리뷰하고 개선점을 알려주세요:\n\n" + code)
                .call()
                .content();

        return new ChatResponse(response);
    }

    // 📝 요약 기능
    public ChatResponse summarize(String text) {
        String response = this.chatClient.prompt()
                .user("다음 텍스트를 한국어로 요약해주세요:\n\n" + text)
                .call()
                .content();

        return new ChatResponse(response);
    }

    // 🎨 창작 도우미
    public ChatResponse generateCreativeContent(String topic, String contentType) {
        String response = this.chatClient.prompt()
                .user("'" + topic + "'을 주제로 " + contentType + "을/를 창작해주세요. 한국어로 작성해주세요.")
                .call()
                .content();

        return new ChatResponse(response);
    }
}