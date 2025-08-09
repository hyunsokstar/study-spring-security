package com.example.security.security_demo.vector.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private String question;       // 물어본 질문
    private List<String> answers;  // 찾은 답변들
    private int foundCount;        // 찾은 개수
}