package com.example.security.security_demo.ai.vector.controller;

import com.example.security.security_demo.ai.vector.dto.*;
import com.example.security.security_demo.ai.vector.service.VectorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/vector")
@RequiredArgsConstructor
@Tag(name = "Vector Store", description = "벡터 스토어 API")
public class VectorController {

    private final VectorService vectorService;

    // ================ 저장 ================

    /**
     * 단일 문서 저장
     */
    @PostMapping("/save")
    @Operation(summary = "문서 저장")
    public ResponseEntity<String> save(@RequestBody SaveRequest request) {
        vectorService.saveDocument(
                request.getText(),
                request.getTitle(),
                request.getDomain(),
                request.getMeta());

        log.info("Document saved - title: {}", request.getTitle());
        return ResponseEntity.ok("저장 완료!");
    }

    /**
     * 대량 문서 저장
     */
    @PostMapping("/save/bulk")
    @Operation(summary = "대량 문서 저장")
    public ResponseEntity<String> saveBulk(@RequestBody SaveBulkRequest request) {
        var items = request.getItems();
        List<String> texts = items.stream().map(SaveRequest::getText).toList();
        List<String> titles = items.stream().map(SaveRequest::getTitle).toList();
        List<Map<String, Object>> metas = items.stream().map(SaveRequest::getMeta).toList();

        vectorService.saveDocuments(texts, titles, "boilerplate", metas);
        log.info("Bulk save completed - count: {}", texts.size());
        return ResponseEntity.ok("대량 저장 완료! (" + texts.size() + ")");
    }

    // ================ 검색 ================

    /**
     * 기본 검색 (content만)
     */
    @PostMapping("/search")
    @Operation(summary = "기본 검색")
    public ResponseEntity<List<String>> search(@RequestBody SearchRequest request) {
        int count = request.getCount() != null ? request.getCount() : 5;
        List<String> results = vectorService.findSimilar(request.getQuestion(), count);

        log.info("Search completed - query: '{}', results: {}", request.getQuestion(), results.size());
        return ResponseEntity.ok(results);
    }

    /**
     * 상세 검색 (메타데이터 포함)
     */
    @PostMapping("/search/detailed")
    @Operation(summary = "상세 검색")
    public ResponseEntity<List<SearchResponse.ProjectInfo>> searchDetailed(@RequestBody SearchRequest request) {
        int count = request.getCount() != null ? request.getCount() : 5;
        List<SearchResponse.ProjectInfo> projects = vectorService.findProjectsWithDetails(
                request.getQuestion(), count);

        log.info("Detailed search completed - query: '{}', found: {} projects",
                request.getQuestion(), projects.size());
        return ResponseEntity.ok(projects);
    }

    // ================ 삭제 ================

    /**
     * 문서 ID로 삭제
     */
    @DeleteMapping("/delete/{documentId}")
    @Operation(summary = "문서 삭제")
    public ResponseEntity<Map<String, Object>> deleteById(@PathVariable String documentId) {
        boolean success = vectorService.deleteByDocumentId(documentId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("documentId", documentId);

        return ResponseEntity.ok(response);
    }

    /**
     * 조건별 삭제
     */
    @PostMapping("/delete")
    @Operation(summary = "조건별 삭제")
    public ResponseEntity<Map<String, Object>> deleteByCondition(@RequestBody DeleteRequest request) {
        Map<String, Object> response = new HashMap<>();

        // 전체 삭제
        if (request.isDeleteAll()) {
            if (!"DELETE_ALL_VECTORS".equals(request.getConfirmCode())) {
                response.put("success", false);
                response.put("message", "confirmCode: 'DELETE_ALL_VECTORS' 필요");
                return ResponseEntity.badRequest().body(response);
            }

            boolean success = vectorService.deleteAllVectors(request.getConfirmCode());
            response.put("success", success);
            response.put("message", success ? "전체 삭제 완료" : "전체 삭제 실패");
            return ResponseEntity.ok(response);
        }

        // 제목으로 삭제
        if (request.getTitle() != null) {
            int deleted = vectorService.deleteByTitle(request.getTitle());
            response.put("success", deleted > 0);
            response.put("deletedCount", deleted);
            return ResponseEntity.ok(response);
        }

        // 도메인으로 삭제
        if (request.getDomain() != null) {
            int deleted = vectorService.deleteByDomain(request.getDomain());
            response.put("success", deleted > 0);
            response.put("deletedCount", deleted);
            return ResponseEntity.ok(response);
        }

        // 문서 ID로 삭제
        if (request.getDocumentId() != null) {
            boolean success = vectorService.deleteByDocumentId(request.getDocumentId());
            response.put("success", success);
            return ResponseEntity.ok(response);
        }

        response.put("success", false);
        response.put("message", "삭제 조건을 지정해주세요");
        return ResponseEntity.badRequest().body(response);
    }

    // ================ 유틸리티 ================

    /**
     * 통계 정보
     */
    @GetMapping("/stats")
    @Operation(summary = "통계 정보")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(vectorService.getVectorStoreStats());
    }

    /**
     * 헬스체크
     */
    @GetMapping("/health")
    @Operation(summary = "헬스체크")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Vector Store Service is healthy");
    }
}