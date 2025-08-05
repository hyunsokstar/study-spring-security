package com.example.security.security_demo.ai.service;

import com.example.security.security_demo.ai.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class ChatGptService {

    private final ChatClient chatClient;

    // 🔥 활성 스트림을 관리하기 위한 Map 추가 (Subscription 사용)
    private final Map<String, AtomicReference<Subscription>> activeStreams = new ConcurrentHashMap<>();

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

    // 🔥 스트리밍 채팅 (실시간 타이핑 효과) - 기존 메서드 유지
    public Flux<String> streamChat(String message) {
        return this.chatClient.prompt()
                .user(message)
                .stream()
                .content()
                .bufferTimeout(10, Duration.ofMillis(100))
                .map(tokens -> String.join("", tokens));
    }

    // 🔥 취소 가능한 스트리밍 채팅 (새로 추가)
    public Flux<String> streamChatWithId(String message, String streamId) {
        AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        activeStreams.put(streamId, subscriptionRef);

        return this.chatClient.prompt()
                .user(message)
                .stream()
                .content()
                .bufferTimeout(10, Duration.ofMillis(100))
                .map(tokens -> String.join("", tokens))
                .doOnSubscribe(subscription -> {
                    // 스트림 시작 시 구독 저장
                    subscriptionRef.set(subscription);
                    log.info("Stream started with ID: {}", streamId);
                })
                .doOnTerminate(() -> {
                    // 스트림 종료 시 제거
                    activeStreams.remove(streamId);
                    log.info("Stream terminated: {}", streamId);
                })
                .doOnCancel(() -> {
                    // 취소 시 제거
                    activeStreams.remove(streamId);
                    log.info("Stream cancelled: {}", streamId);
                })
                .doOnError(error -> {
                    // 에러 발생 시 제거
                    activeStreams.remove(streamId);
                    log.error("Stream error: {}", streamId, error);
                });
    }

    // 🛑 스트리밍 취소 메서드 (새로 추가)
    public boolean cancelStream(String streamId) {
        AtomicReference<Subscription> subscriptionRef = activeStreams.get(streamId);
        if (subscriptionRef != null) {
            Subscription subscription = subscriptionRef.get();
            if (subscription != null) {
                subscription.cancel();
                activeStreams.remove(streamId);
                log.info("Stream cancelled by user: {}", streamId);
                return true;
            }
        }
        log.warn("Stream not found or already cancelled: {}", streamId);
        return false;
    }

    // 📊 활성 스트림 목록 조회 (디버깅용)
    public Set<String> getActiveStreams() {
        return activeStreams.keySet();
    }

    // 🧹 모든 스트림 정리 (애플리케이션 종료 시)
    public void cancelAllStreams() {
        activeStreams.forEach((id, subscriptionRef) -> {
            Subscription subscription = subscriptionRef.get();
            if (subscription != null) {
                subscription.cancel();
            }
        });
        activeStreams.clear();
        log.info("All streams cancelled");
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