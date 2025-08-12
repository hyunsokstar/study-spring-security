package com.example.security.security_demo.chatting.presentation;

import com.example.security.security_demo.chatting.presentation.dto.ChattingRoomResponse;
import com.example.security.security_demo.chatting.presentation.dto.CreateChattingRoomRequest;
import com.example.security.security_demo.chatting.service.ChattingRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chatting/rooms")
@RequiredArgsConstructor
public class ChattingRoomController {
    private final ChattingRoomService roomService;

    @PostMapping
    public ResponseEntity<?> createRoom(@Valid @RequestBody CreateChattingRoomRequest req) {
        try {
            // 현재 로그인한 사용자 정보 추출
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(401)
                    .body(new ErrorResponse("로그인이 필요합니다. 채팅방을 생성하려면 먼저 로그인해주세요."));
            }
            
            String username = authentication.getName();
            ChattingRoomResponse response = roomService.createRoom(req, username);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404)
                .body(new ErrorResponse("사용자를 찾을 수 없습니다: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ErrorResponse("채팅방 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 에러 응답용 DTO
    public static class ErrorResponse {
        private final String message;
        private final long timestamp;
        
        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }

    @GetMapping
    public ResponseEntity<List<ChattingRoomResponse>> listRooms() {
        return ResponseEntity.ok(roomService.listRooms());
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable String roomId) {
        try {
            // 현재 로그인한 사용자 정보 추출
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(401)
                    .body(new ErrorResponse("로그인이 필요합니다. 채팅방을 삭제하려면 먼저 로그인해주세요."));
            }
            
            String username = authentication.getName();
            java.util.UUID uuid = java.util.UUID.fromString(roomId);
            
            roomService.deleteRoom(uuid, username);
            return ResponseEntity.ok().body(new SuccessResponse("채팅방이 성공적으로 삭제되었습니다."));
            
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("UUID")) {
                return ResponseEntity.status(400)
                    .body(new ErrorResponse("잘못된 채팅방 ID 형식입니다."));
            }
            return ResponseEntity.status(404)
                .body(new ErrorResponse(e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ErrorResponse("채팅방 삭제 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
    
    // 성공 응답용 DTO
    public static class SuccessResponse {
        private final String message;
        private final long timestamp;
        
        public SuccessResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}
