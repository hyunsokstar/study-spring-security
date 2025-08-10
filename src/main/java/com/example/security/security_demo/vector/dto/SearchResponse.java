package com.example.security.security_demo.vector.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private String question;       // 물어본 질문
    private List<String> answers;  // 찾은 답변들 (기존 - 하위호환성)
    private int foundCount;        // 찾은 개수

    // ===== 추가된 필드 =====
    private List<ProjectInfo> projects;  // 구조화된 프로젝트 정보
    private String summary;              // 자연어 요약 답변

    @Data
    @Builder
    public static class ProjectInfo {
        private String id;              // 👈 이 필드 추가
        private String title;
        private String description;
        private String githubUrl;
        private List<String> stack;
        private List<String> tags;
        private String author;
        private String version;
        // private Map<String, Object> additionalInfo; // 👈 이 필드 제거
    }
}