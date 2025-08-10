package com.example.security.security_demo.vector.dto;

import lombok.Data;

@Data
public class DeleteRequest {
    private String documentId;     // 특정 문서 ID (선택)
    private String title;          // 제목으로 삭제 (선택)
    private String domain;         // 도메인별 삭제 (선택)
    private boolean deleteAll;     // 전체 삭제 플래그
    private String confirmCode;    // 전체 삭제 시 확인 코드 (안전장치)
}