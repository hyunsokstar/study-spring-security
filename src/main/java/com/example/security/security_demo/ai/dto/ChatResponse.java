package com.example.security.security_demo.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String response;

    // 줄바꿈을 HTML <br>로 변환한 메시지 반환
    @JsonProperty("formattedResponse")
    public String getFormattedResponse() {
        if (response == null) return "";
        return response
                .replace("\n\n", "<br><br>")  // 문단 구분
                .replace("\n", "<br>");        // 일반 줄바꿈
    }

    // 마크다운 형식을 HTML로 변환 (선택사항)
    @JsonProperty("htmlResponse")
    public String getHtmlResponse() {
        if (response == null) return "";
        return response
                .replace("\n\n", "</p><p>")   // 문단 태그로 변환
                .replace("\n", "<br>")
                .replaceAll("^", "<p>")        // 시작 태그
                .replaceAll("$", "</p>");      // 종료 태그
    }
}