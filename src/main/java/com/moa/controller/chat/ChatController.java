package com.moa.controller.chat;

import com.moa.annotation.CurrentUserId;
import com.moa.dto.chat.ChatHistoryResponse;
import com.moa.dto.chat.ReceiptResponse;
import com.moa.entity.ChatModeType;
import com.moa.exception.InvalidImageException;
import com.moa.reponse.AiReceiptResponse;
import com.moa.service.chat.ChatService;
import com.moa.service.chat.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final TransactionService transactionService;

    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "AI 챗봇에게 메시지 전송",
            description = "텍스트 메시지와 영수증 이미지를 전송합니다. (이미지는 선택)" +
                    "mode: RECEIPT(내역모드) or CHAT(대화모드)")
    public ResponseEntity<ReceiptResponse> sendMessage(
            @CurrentUserId Long userId,
            @RequestPart(value = "message", required = false) String message,
            @RequestPart("mode") String mode,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        if (mode == null || mode.isEmpty() || !(ChatModeType.RECEIPT.getText().equals(mode) || ChatModeType.CHAT.getText().equals(mode))) {
            String errorMessage = "mode는 'RECEIPT' 또는 'CHAT'이어야 합니다.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ReceiptResponse.builder().message(errorMessage).build());
        }

        if (image != null) {
            validateImage(image);
        }

        try {
            log.info("사용자 {} 메시지 전송 요청: {}, 이미지 첨부: {}", userId, message, image != null);

            if (ChatModeType.RECEIPT.getText().equals(mode)) {
                AiReceiptResponse response = chatService.sendReceiptMessage(userId, message, image);
//                Long transactionId = transactionService.addTransactionInfo(userId, response.request());

                return ResponseEntity.ok(
                        new ReceiptResponse(
                                response.message(),
                                response.request()
//                                transactionService.getTransaction(userId, transactionId)
                        )
                );
            } else {
                ReceiptResponse response = chatService.sendMessage(userId, message, mode, image);
                return ResponseEntity.ok(response);
            }
        } catch (InvalidImageException e) {
            log.warn("이미지 검증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ReceiptResponse.builder().message(e.getMessage()).build());
        } catch (Exception e) {
            log.error("메시지 전송 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private void validateImage(MultipartFile image) {
        // 크기 검증 (최대 5MB)
        if (image.getSize() > 5 * 1024 * 1024) {
            throw new InvalidImageException("이미지 크기는 5MB를 초과할 수 없습니다.");
        }

        // 타입 검증 (JPEG, PNG만 허용)
        String contentType = image.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new InvalidImageException("지원되는 이미지 형식은 JPEG와 PNG입니다.");
        }

        // 파일이 비어있는지 검증
        if (image.isEmpty()) {
            throw new InvalidImageException("빈 이미지 파일은 전송할 수 없습니다.");
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

    @GetMapping("/greetingByTime")
    @Operation(summary = "시간대별 인사", description = "캐릭터의 시간대별 인사 메시지를 반환합니다.")
    public ResponseEntity<ChatHistoryResponse> getGreetingByTime(@CurrentUserId Long userId) {
        try {
            ChatHistoryResponse response = chatService.getGreetingByTime(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("시간대별 인사 요청 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
