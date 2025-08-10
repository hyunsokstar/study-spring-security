package com.example.security.security_demo.ai.vector.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

@Data
public class SaveRequest {
  @NotBlank
  private String text;             // 저장할 텍스트 (README 요약, 사용법, 핵심 포인트 등)

  private String title;            // 제목 (예: "Next15+Shadcn Auth 보일러플레이트")

  // 지금은 보일러플레이트만 저장하더라도, 확장성을 위해 필드 유지
  // 요청에서 비우면 서비스에서 "boilerplate"로 기본값 처리 추천
  private String domain;           // 예: "boilerplate"

  // 자유 확장 메타: 검색/권한/출처 필터링에 씀
  // 예시 키: stack, scope, version, owner, repoUrl, path, tags[], security_level
  private Map<String, Object> meta;
}
