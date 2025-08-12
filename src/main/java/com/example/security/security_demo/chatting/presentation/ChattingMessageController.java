package com.example.security.security_demo.chatting.presentation;

import com.example.security.security_demo.chatting.presentation.dto.ChattingMessageResponse;
import com.example.security.security_demo.chatting.presentation.dto.SendChattingMessageRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import com.example.security.security_demo.chatting.service.ChattingMessageService;

@RestController
@RequestMapping("/api/chatting/rooms/{roomId}/messages")
@RequiredArgsConstructor
public class ChattingMessageController {
    private final ChattingMessageService messageService;

    @PostMapping
    public ResponseEntity<ChattingMessageResponse> sendMessage(
            @PathVariable UUID roomId, @Valid @RequestBody SendChattingMessageRequest req) {
        return ResponseEntity.ok(messageService.sendMessage(roomId, req));
    }

    @GetMapping
    public ResponseEntity<Page<ChattingMessageResponse>> listMessages(
            @PathVariable UUID roomId,
            @ParameterObject @RequestParam(defaultValue = "0") int page,
            @ParameterObject @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(messageService.listMessages(roomId, page, size));
    }
}