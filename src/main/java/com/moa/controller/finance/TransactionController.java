package com.moa.controller.finance;

import com.moa.annotation.CurrentUserId;
import com.moa.dto.chat.ChatResponse.TransactionInfo;
import com.moa.service.chat.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 거래내역 컨트롤러
 */
@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction", description = "가계부 거래내역 API")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/add")
    @Operation(summary = "거래 내역 추가(AI Response)", description = "거래 내역을 추가합니다.")
    public ResponseEntity<Map<String, String>> add(
            @CurrentUserId Long userId,
            @RequestBody TransactionInfo transactionInfo) {
        try {
            log.info("사용자 {} 가계부 내역 추가 요청: {}", userId, transactionInfo);
            Long transactionId = transactionService.addTransactionInfo(userId, transactionInfo);
            return ResponseEntity.ok(Map.of("message", "가계부 내역이 추가되었습니다.", "transactionId", ""+transactionId));
        } catch (Exception e) {
            log.error("가계부 내역 추가 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/select/{transactionId}")
    @Operation(summary = "거래 내역 조회", description = "거래 내역을 ID로 조회합니다.")
    public ResponseEntity<?> get(
            @CurrentUserId Long userId,
            @PathVariable Long transactionId) {
        try {
            log.info("사용자 {} 가계부 내역 {} 조회 요청", userId, transactionId);
            TransactionInfo transactionInfo = transactionService.getTransaction(userId, transactionId);
            return ResponseEntity.ok(transactionInfo);
        } catch (RuntimeException e) {
            log.error("가계부 내역 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("가계부 내역 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/update/{transactionId}")
    @Operation(summary = "거래 내역 수정", description = "거래 내역을 수정합니다.")
    public ResponseEntity<Map<String, String>> update(
            @CurrentUserId Long userId,
            @PathVariable Long transactionId,
            @RequestBody TransactionInfo transactionInfo) {
        try {
            log.info("사용자 {} 가계부 내역 {} 수정 요청", userId, transactionId);
            transactionService.updateTransaction(userId, transactionId, transactionInfo);
            return ResponseEntity.ok(Map.of("message", "가계부 내역이 수정되었습니다."));
        } catch (RuntimeException e) {
            log.error("가계부 내역 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("가계부 내역 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{transactionId}")
    @Operation(summary = "거래 내역 삭제", description = "거래 내역을 삭제합니다.")
    public ResponseEntity<Map<String, String>> delete(
            @CurrentUserId Long userId,
            @PathVariable Long transactionId) {
        try {
            log.info("사용자 {} 가계부 내역 {} 삭제 요청", userId, transactionId);
            transactionService.deleteTransaction(userId, transactionId);
            return ResponseEntity.ok(Map.of("message", "가계부 내역이 삭제되었습니다."));
        } catch (RuntimeException e) {
            log.error("가계부 내역 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("가계부 내역 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
