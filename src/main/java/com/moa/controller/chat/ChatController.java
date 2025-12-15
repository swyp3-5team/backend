package com.moa.controller.chat;

import com.moa.annotation.CurrentUserId;
import com.moa.dto.chat.ChatHistoryResponse;
import com.moa.dto.chat.ChatRequest;
import com.moa.dto.chat.ChatResponse;
import com.moa.service.chat.ChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 채팅 컨트롤러
 * HyperCLOVA X 기반 감정 기반 가계부 AI 챗봇 API
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Chat", description = "감정 기반 AI 챗봇 API")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    @Operation(summary = "AI 챗봇에게 메시지 전송", description = "사용자 메시지를 보내고 AI 응답을 받습니다. 거래내역이 포함된 경우 JSON 형식으로 파싱됩니다.")
    public ResponseEntity<ChatResponse> sendMessage(
            @CurrentUserId Long userId,
            @RequestBody ChatRequest request) {
        try {
            log.info("사용자 {} 메시지 전송 요청: {}", userId, request.getMessage());
            ChatResponse response = chatService.sendMessage(userId, request.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("메시지 전송 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/history")
    @Operation(summary = "대화 히스토리 조회", description = "사용자의 대화 히스토리를 페이징하여 조회합니다.")
    public ResponseEntity<List<ChatHistoryResponse>> getChatHistory(
            @CurrentUserId Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("사용자 {} 대화 히스토리 조회 - 페이지: {}, 크기: {}", userId, page, size);
            List<ChatHistoryResponse> history = chatService.getChatHistory(userId, page, size);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("히스토리 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/history")
    @Operation(summary = "대화 히스토리 삭제", description = "사용자의 모든 대화 히스토리를 삭제합니다.")
    public ResponseEntity<Map<String, String>> deleteChatHistory(@CurrentUserId Long userId) {
        try {
            log.info("사용자 {} 대화 히스토리 삭제 요청", userId);
            chatService.deleteChatHistory(userId);
            return ResponseEntity.ok(Map.of("message", "대화 히스토리가 삭제되었습니다."));
        } catch (Exception e) {
            log.error("히스토리 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
