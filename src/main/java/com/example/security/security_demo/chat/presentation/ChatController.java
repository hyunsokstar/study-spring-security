package com.example.security.security_demo.chat.presentation;

import com.example.security.security_demo.chat.application.ChatCommandService;
import com.example.security.security_demo.chat.application.ChatQueryService;
import com.example.security.security_demo.chat.domain.ChatRoom;
import com.example.security.security_demo.chat.domain.Message;
import com.example.security.security_demo.chat.presentation.dto.CreateRoomRequest;
import com.example.security.security_demo.chat.presentation.dto.PostMessageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatCommandService commandService;
    private final ChatQueryService queryService;

    // 방 생성
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoom> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        ChatRoom room = commandService.createRoom(request.getName());
        return ResponseEntity.created(URI.create("/api/chat/rooms/" + room.getId())).body(room);
    }

    // 방 목록
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoom>> listRooms() {
        return ResponseEntity.ok(queryService.listRooms());
    }

    // 방 상세
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoom> getRoom(@PathVariable UUID roomId) {
        return ResponseEntity.ok(queryService.getRoom(roomId));
    }

    // 메시지 전송
    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Message> postMessage(@PathVariable UUID roomId,
                                               @Valid @RequestBody PostMessageRequest request) {
        Message message = commandService.postMessage(roomId, request.getSenderUserId(), request.getContent());
        return ResponseEntity.ok(message);
    }

    // 메시지 조회
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable UUID roomId) {
        return ResponseEntity.ok(queryService.getMessages(roomId));
    }
}
