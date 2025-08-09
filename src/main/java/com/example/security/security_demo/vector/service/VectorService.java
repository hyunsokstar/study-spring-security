package com.example.security.security_demo.vector.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class VectorService {

    @Autowired
    private VectorStore vectorStore;

    /**
     * 텍스트 저장하기
     */
    public void saveText(String text, String title) {
        Document doc = new Document(
                text,
                Map.of("title", title != null ? title : "제목없음")
        );

        vectorStore.add(List.of(doc));
    }

    /**
     * 비슷한 텍스트 찾기
     */
    public List<String> findSimilar(String question, int count) {
        SearchRequest searchRequest = SearchRequest.query(question)
                .withTopK(count);

        return vectorStore.similaritySearch(searchRequest)
                .stream()
                .map(Document::getContent)
                .toList();
    }
}