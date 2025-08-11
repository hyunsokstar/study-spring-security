package com.example.security.security_demo.chatting.presentation;

import com.example.security.security_demo.chatting.presentation.dto.ChattingRoomResponse;
import com.example.security.security_demo.chatting.presentation.dto.CreateChattingRoomRequest;
import com.example.security.security_demo.chatting.service.ChattingRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatting/rooms")
@RequiredArgsConstructor
public class ChattingRoomController {
    private final ChattingRoomService roomService;

    @PostMapping
    public ResponseEntity<ChattingRoomResponse> createRoom(@Valid @RequestBody CreateChattingRoomRequest req) {
        return ResponseEntity.ok(roomService.createRoom(req));
    }

    @GetMapping
    public ResponseEntity<List<ChattingRoomResponse>> listRooms() {
        return ResponseEntity.ok(roomService.listRooms());
    }
}
