package com.example.security.security_demo.ai.chatbot.service;

import com.example.security.security_demo.ai.chatbot.dto.ChatResponse;
import com.example.security.security_demo.ai.vector.service.VectorService;
import com.example.security.security_demo.ai.vector.dto.SearchResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class ChatGptService {

    private final ChatClient chatClient;
    private final VectorService vectorService; // 🔥 벡터 서비스 추가

    // 활성 스트림을 관리하기 위한 Map
    private final Map<String, AtomicReference<Subscription>> activeStreams = new ConcurrentHashMap<>();

    public ChatGptService(ChatClient.Builder chatClientBuilder, VectorService vectorService) {
        this.vectorService = vectorService;
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                당신은 한국어로 친절하게 답변하는 AI 어시스턴트입니다. 
                사용자의 질문에 대해 관련된 프로젝트나 보일러플레이트 정보를 바탕으로 정확하고 도움이 되는 답변을 제공해주세요.
                
                답변 시 다음 형식을 따라주세요:
                - 문단과 문단 사이는 빈 줄로 구분
                - 긴 설명은 여러 문단으로 나누어 가독성 향상
                - 목록이 필요한 경우 번호나 불릿 포인트 사용
                - 코드는 백틱(```)으로 감싸서 표시
                - GitHub URL이 있다면 링크로 제공
                - 기술스택과 태그 정보를 활용하여 구체적인 설명 제공
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

    // 🔥 벡터 검색 기반 일반 채팅
    public ChatResponse chatWithVector(String message) {
        return chatWithVector(message, 5); // 기본 5개 문서
    }

    public ChatResponse chatWithVector(String message, int vectorCount) {
        // 1️⃣ 벡터 검색으로 관련 문서 찾기
        List<SearchResponse.ProjectInfo> relevantProjects = vectorService.findProjectsWithDetails(message, vectorCount);

        // 2️⃣ 검색 결과를 컨텍스트로 구성
        String context = buildContextFromProjects(relevantProjects);

        // 3️⃣ 컨텍스트와 함께 프롬프트 생성
        String enhancedPrompt = buildEnhancedPrompt(message, context);

        log.info("Vector search completed: found {} projects for query: '{}'", relevantProjects.size(), message);

        // 4️⃣ ChatGPT 호출
        String response = this.chatClient.prompt()
                .user(enhancedPrompt)
                .call()
                .content();

        return new ChatResponse(response);
    }

    // 🔥 기존 스트리밍 채팅 (벡터 검색 없음)
    public Flux<String> streamChat(String message) {
        return this.chatClient.prompt()
                .user(message)
                .stream()
                .content()
                .bufferTimeout(10, Duration.ofMillis(100))
                .map(tokens -> String.join("", tokens));
    }

    // 🔥 벡터 검색 기반 스트리밍 채팅 (메인 메서드)
    public Flux<String> streamChatWithVector(String message, String streamId) {
        return streamChatWithVector(message, streamId, 5); // 기본 5개 문서
    }

    // 🔥 벡터 검색 기반 스트리밍 채팅 (문서 개수 지정)
    public Flux<String> streamChatWithVector(String message, String streamId, int vectorCount) {
        AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
        activeStreams.put(streamId, subscriptionRef);

        // 1️⃣ 벡터 검색으로 관련 문서 찾기
        List<SearchResponse.ProjectInfo> relevantProjects = vectorService.findProjectsWithDetails(message, vectorCount);

        // 2️⃣ 검색 결과를 컨텍스트로 구성
        String context = buildContextFromProjects(relevantProjects);

        // 3️⃣ 컨텍스트와 함께 프롬프트 생성
        String enhancedPrompt = buildEnhancedPrompt(message, context);

        log.info("Vector search completed for stream {}: found {} projects", streamId, relevantProjects.size());

        // 4️⃣ ChatGPT 스트리밍 실행
        return this.chatClient.prompt()
                .user(enhancedPrompt)
                .stream()
                .content()
                .bufferTimeout(10, Duration.ofMillis(100))
                .map(tokens -> String.join("", tokens))
                .doOnSubscribe(subscription -> {
                    subscriptionRef.set(subscription);
                    log.info("Vector-based stream started with ID: {}", streamId);
                })
                .doOnTerminate(() -> {
                    activeStreams.remove(streamId);
                    log.info("Vector-based stream terminated: {}", streamId);
                })
                .doOnCancel(() -> {
                    activeStreams.remove(streamId);
                    log.info("Vector-based stream cancelled: {}", streamId);
                })
                .doOnError(error -> {
                    activeStreams.remove(streamId);
                    log.error("Vector-based stream error: {}", streamId, error);
                });
    }

    // 🔥 취소 가능한 스트리밍 채팅 (기존 방식, 벡터 검색 없음)
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
                    subscriptionRef.set(subscription);
                    log.info("Stream started with ID: {}", streamId);
                })
                .doOnTerminate(() -> {
                    activeStreams.remove(streamId);
                    log.info("Stream terminated: {}", streamId);
                })
                .doOnCancel(() -> {
                    activeStreams.remove(streamId);
                    log.info("Stream cancelled: {}", streamId);
                })
                .doOnError(error -> {
                    activeStreams.remove(streamId);
                    log.error("Stream error: {}", streamId, error);
                });
    }

    // 📝 검색된 프로젝트들을 컨텍스트로 변환
    private String buildContextFromProjects(List<SearchResponse.ProjectInfo> projects) {
        if (projects.isEmpty()) {
            return "관련된 프로젝트나 보일러플레이트를 찾을 수 없습니다.";
        }

        StringBuilder context = new StringBuilder();
        context.append("다음은 검색된 관련 프로젝트/보일러플레이트 정보입니다:\n\n");

        for (int i = 0; i < projects.size(); i++) {
            SearchResponse.ProjectInfo project = projects.get(i);
            context.append(String.format("## %d. %s\n", i + 1, project.getTitle()));
            context.append(String.format("**설명**: %s\n", project.getDescription()));

            if (project.getGithubUrl() != null && !project.getGithubUrl().isEmpty()) {
                context.append(String.format("**GitHub**: %s\n", project.getGithubUrl()));
            }

            if (project.getStack() != null && !project.getStack().isEmpty()) {
                context.append(String.format("**기술스택**: %s\n", String.join(", ", project.getStack())));
            }

            if (project.getTags() != null && !project.getTags().isEmpty()) {
                context.append(String.format("**태그**: %s\n", String.join(", ", project.getTags())));
            }

            if (project.getAuthor() != null && !project.getAuthor().isEmpty()) {
                context.append(String.format("**작성자**: %s\n", project.getAuthor()));
            }

            if (project.getVersion() != null && !project.getVersion().isEmpty()) {
                context.append(String.format("**버전**: %s\n", project.getVersion()));
            }

            context.append("\n");
        }

        return context.toString();
    }

    // 🎯 향상된 프롬프트 생성
    private String buildEnhancedPrompt(String userMessage, String context) {
        return String.format("""
            사용자 질문: %s
            
            %s
            
            위의 검색 결과를 바탕으로 사용자의 질문에 대해 구체적이고 도움이 되는 답변을 제공해주세요.
            
            답변 가이드라인:
            - 검색된 프로젝트들 중에서 사용자 질문과 가장 관련 있는 것들을 우선으로 설명
            - GitHub URL이 있다면 "이 프로젝트는 여기에서 확인할 수 있습니다: [URL]" 형태로 안내
            - 기술스택과 태그 정보를 활용하여 구체적인 설명 제공
            - 만약 완벽히 맞는 프로젝트가 없다면, 가장 유사한 것들을 조합하여 대안 제시
            - 프로젝트 사용법이나 설치 방법에 대한 실용적인 조언 포함
            - 한국어로 친절하고 자세하게 설명
            """, userMessage, context);
    }

    // 🛑 스트리밍 취소 메서드
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