package com.example.security.security_demo.ai.vector.service;

import com.example.security.security_demo.ai.vector.dto.SearchResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class VectorService {

    private static final Logger log = LoggerFactory.getLogger(VectorService.class);

    @Autowired
    private VectorStore vectorStore;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;  // 직접 DB 접근용 (전체 삭제 시)

    /**
     * 텍스트와 메타데이터 저장하기 (전체 SaveRequest 활용)
     */
    public void saveDocument(String text, String title, String domain, Map<String, Object> meta) {
        // 메타데이터 준비
        Map<String, Object> metadata = new HashMap<>();

        // 기본 메타데이터
        String documentId = UUID.randomUUID().toString();
        metadata.put("id", documentId);
        metadata.put("title", title != null ? title : "제목없음");
        metadata.put("domain", domain != null ? domain : "boilerplate");
        metadata.put("timestamp", System.currentTimeMillis());

        // 추가 메타데이터 병합
        if (meta != null) {
            metadata.putAll(meta);
        }

        // Document 생성
        Document doc = new Document(
                documentId,  // ID 지정
                text,
                metadata);

        // 벡터 스토어에 저장
        vectorStore.add(List.of(doc));

        log.info("Document saved - id: {}, title: {}, domain: {}, metaKeys: {}",
                documentId, title, domain, meta != null ? meta.keySet() : "none");
    }

    /**
     * 간단한 텍스트 저장 (하위 호환성)
     */
    public void saveText(String text, String title) {
        saveDocument(text, title, "boilerplate", null);
    }

    /**
     * 대량 저장: 요청 리스트를 Document 리스트로 변환하여 한 번에 저장
     */
    public void saveDocuments(List<String> texts, List<String> titles, String domain,
                              List<Map<String, Object>> metas) {
        if (texts == null || texts.isEmpty())
            return;
        int size = texts.size();

        List<Document> docs = java.util.stream.IntStream.range(0, size).mapToObj(i -> {
            String documentId = UUID.randomUUID().toString();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("id", documentId);
            metadata.put("title", titles != null && i < titles.size() ? titles.get(i) : "제목없음");
            metadata.put("domain", domain != null ? domain : "boilerplate");
            metadata.put("timestamp", System.currentTimeMillis());
            if (metas != null && i < metas.size() && metas.get(i) != null) {
                metadata.putAll(metas.get(i));
            }
            return new Document(documentId, texts.get(i), metadata);
        }).collect(Collectors.toList());

        vectorStore.add(docs);
        log.info("Bulk documents saved - count: {}", docs.size());
    }

    /**
     * 비슷한 텍스트 찾기 (컨텐츠만 반환)
     */
    public List<String> findSimilar(String question, int count) {
        SearchRequest searchRequest = SearchRequest.query(question)
                .withTopK(count)
                .withSimilarityThreshold(0.5);

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        log.info("Search completed - query: '{}', found: {} documents",
                question, documents.size());

        return documents.stream()
                .map(Document::getContent)
                .toList();
    }

    /**
     * 메타데이터 포함 검색
     */
    public List<Document> findSimilarWithMetadata(String question, int count) {
        SearchRequest searchRequest = SearchRequest.query(question)
                .withTopK(count)
                .withSimilarityThreshold(0.5);

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        log.info("Search with metadata - query: '{}', found: {} documents",
                question, documents.size());

        return documents;
    }

    /**
     * 구조화된 프로젝트 정보로 검색 결과 반환
     */
    public List<SearchResponse.ProjectInfo> findProjectsWithDetails(String question, int count) {
        SearchRequest searchRequest = SearchRequest.query(question)
                .withTopK(count)
                .withSimilarityThreshold(0.5);

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        log.info("Search with details - query: '{}', found: {} documents",
                question, documents.size());

        return documents.stream()
                .map(this::convertToProjectInfo)
                .toList();
    }

    /**
     * Document를 ProjectInfo로 변환 (최종 버전: id 포함, additionalInfo 제거)
     */
    private SearchResponse.ProjectInfo convertToProjectInfo(Document doc) {
        Map<String, Object> meta = doc.getMetadata();

        // 안전한 메타데이터 처리
        if (meta == null) {
            meta = new HashMap<>();
        }

        return SearchResponse.ProjectInfo.builder()
                .id(safeGetString(meta, "id", null))  // 🔥 id 필드 추가
                .description(doc.getContent() != null ? doc.getContent() : "")
                .title(safeGetString(meta, "title", "제목없음"))
                .githubUrl(safeGetString(meta, "githubUrl", null))
                .author(safeGetString(meta, "author", null))
                .version(safeGetString(meta, "version", null))
                .tags(extractStringList(meta.get("tags")))        // 🔥 실제 데이터 추출
                .stack(extractStringList(meta.get("stack")))      // 🔥 실제 데이터 추출
                // 🔥 additionalInfo 완전 제거
                .build();
    }

    /**
     * 안전하게 String 값 추출
     */
    private String safeGetString(Map<String, Object> map, String key, String defaultValue) {
        try {
            Object value = map.get(key);
            if (value == null) {
                return defaultValue;
            }
            return value.toString();
        } catch (Exception e) {
            log.warn("Failed to extract string for key: {}, error: {}", key, e.getMessage());
            return defaultValue;
        }
    }

    /**
     * Object를 안전하게 List<String>으로 변환
     */
    private List<String> extractStringList(Object obj) {
        if (obj == null) {
            return new ArrayList<>();
        }

        try {
            if (obj instanceof List<?>) {
                List<?> list = (List<?>) obj;
                return list.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Failed to extract string list: {}", e.getMessage());
        }

        return new ArrayList<>();
    }

    // ================ 삭제 기능 ================

    /**
     * 문서 ID로 삭제
     */
    public boolean deleteByDocumentId(String documentId) {
        try {
            vectorStore.delete(List.of(documentId));
            log.info("Document deleted - id: {}", documentId);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete document - id: {}", documentId, e);
            return false;
        }
    }

    /**
     * 여러 문서 ID로 삭제
     */
    public int deleteByDocumentIds(List<String> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return 0;
        }

        try {
            vectorStore.delete(documentIds);
            log.info("Documents deleted - count: {}", documentIds.size());
            return documentIds.size();
        } catch (Exception e) {
            log.error("Failed to delete documents", e);
            return 0;
        }
    }

    /**
     * 제목으로 문서 찾아서 삭제
     */
    @Transactional
    public int deleteByTitle(String title) {
        SearchRequest searchRequest = SearchRequest.query(title)
                .withTopK(100);

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        List<String> idsToDelete = documents.stream()
                .filter(doc -> {
                    String docTitle = (String) doc.getMetadata().get("title");
                    return title.equals(docTitle);
                })
                .map(Document::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (idsToDelete.isEmpty()) {
            log.info("No documents found with title: {}", title);
            return 0;
        }

        return deleteByDocumentIds(idsToDelete);
    }

    /**
     * 도메인별 삭제
     */
    @Transactional
    public int deleteByDomain(String domain) {
        SearchRequest searchRequest = SearchRequest.query("")
                .withTopK(1000);

        List<Document> documents = vectorStore.similaritySearch(searchRequest);

        List<String> idsToDelete = documents.stream()
                .filter(doc -> {
                    String docDomain = (String) doc.getMetadata().get("domain");
                    return domain.equals(docDomain);
                })
                .map(Document::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (idsToDelete.isEmpty()) {
            log.info("No documents found with domain: {}", domain);
            return 0;
        }

        log.info("Deleting {} documents from domain: {}", idsToDelete.size(), domain);
        return deleteByDocumentIds(idsToDelete);
    }

    /**
     * 전체 벡터 데이터 삭제 (위험! 테스트용)
     */
    @Transactional
    public boolean deleteAllVectors(String confirmCode) {
        if (!"DELETE_ALL_VECTORS".equals(confirmCode)) {
            log.warn("Invalid confirmation code for deleteAll operation");
            return false;
        }

        try {
            if (jdbcTemplate != null) {
                int deleted = jdbcTemplate.update("TRUNCATE TABLE vector_store");
                log.warn("⚠️ ALL VECTOR DATA DELETED - TRUNCATE executed");
                return true;
            } else {
                SearchRequest searchRequest = SearchRequest.query("")
                        .withTopK(10000);

                List<Document> allDocs = vectorStore.similaritySearch(searchRequest);
                List<String> allIds = allDocs.stream()
                        .map(Document::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                if (!allIds.isEmpty()) {
                    vectorStore.delete(allIds);
                    log.warn("⚠️ ALL VECTOR DATA DELETED - {} documents", allIds.size());
                }
                return true;
            }
        } catch (Exception e) {
            log.error("Failed to delete all vectors", e);
            return false;
        }
    }

    /**
     * 벡터 스토어 통계 정보
     */
    public Map<String, Object> getVectorStoreStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            if (jdbcTemplate != null) {
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM vector_store",
                        Integer.class
                );
                stats.put("totalDocuments", count);

                List<Map<String, Object>> domainCounts = jdbcTemplate.queryForList(
                        "SELECT metadata->>'domain' as domain, COUNT(*) as count " +
                                "FROM vector_store " +
                                "GROUP BY metadata->>'domain'"
                );
                stats.put("domainCounts", domainCounts);
            } else {
                stats.put("totalDocuments", "N/A");
                stats.put("message", "JdbcTemplate not available");
            }
        } catch (Exception e) {
            log.error("Failed to get stats", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }
}