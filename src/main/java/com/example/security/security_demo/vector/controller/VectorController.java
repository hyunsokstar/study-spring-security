package com.example.security.security_demo.vector.controller;

import com.example.security.security_demo.vector.dto.SaveRequest;
import com.example.security.security_demo.vector.dto.SearchRequest;
import com.example.security.security_demo.vector.dto.SearchResponse;
import com.example.security.security_demo.vector.service.VectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vector")
@RequiredArgsConstructor
@Tag(name = "Vector Store", description = "벡터 스토어 API")
public class VectorController {

    private final VectorService vectorService;

    @PostMapping("/save")
    @Operation(summary = "텍스트 저장", description = "텍스트를 벡터로 변환하여 저장합니다")
    public ResponseEntity<String> save(@RequestBody SaveRequest request) {
        vectorService.saveText(request.getText(), request.getTitle());
        return ResponseEntity.ok("저장 완료!");
    }

    @PostMapping("/search")
    @Operation(summary = "유사 텍스트 검색", description = "질문과 비슷한 텍스트를 찾습니다")
    public ResponseEntity<SearchResponse> search(@RequestBody SearchRequest request) {
        List<String> results = vectorService.findSimilar(
                request.getQuestion(),
                request.getCount() != null ? request.getCount() : 5
        );

        SearchResponse response = new SearchResponse();
        response.setQuestion(request.getQuestion());
        response.setAnswers(results);
        response.setFoundCount(results.size());

        return ResponseEntity.ok(response);
    }
}