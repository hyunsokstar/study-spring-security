package com.example.security.security_demo.vector.dto;

import lombok.Data;

@Data
public class SearchRequest {
    private String question;      // 검색할 질문
    private Integer count = 5;    // 찾을 개수 (기본값: 5개)
}