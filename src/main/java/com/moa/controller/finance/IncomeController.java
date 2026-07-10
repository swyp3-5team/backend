package com.moa.controller.finance;

import com.moa.annotation.CurrentUserId;
import com.moa.dto.IncomeCreateRequest;
import com.moa.dto.MessageResponse;
import com.moa.dto.TransactionGroupInfo;
import com.moa.service.chat.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/transaction-groups/income")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Income", description = "수입 내역 API")
public class IncomeController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "수입 내역 추가", description = "수입 내역을 추가 API")
    public ResponseEntity<MessageResponse<Long>> add(
            @CurrentUserId Long userId,
            @RequestBody IncomeCreateRequest request) {
        try {
            log.info("사용자 {} 수입 내역 추가 요청: {}", userId, request);
            Long incomeId = transactionService.addIncomeInfo(userId, request);
            return ResponseEntity.ok(MessageResponse.of("수입 내역이 추가되었습니다.", incomeId));
        } catch (Exception e) {
            log.error("수입 내역 추가 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(MessageResponse.of(e.getMessage(), null));
        }
    }

    @GetMapping
    @Operation(summary = "수입 기록 연-월 조회", description = "수입 기록을 연-월 기준으로 조회하는 API")
    public ResponseEntity<List<TransactionGroupInfo>> getIncomesByYearMonth(
            @CurrentUserId Long userId,
            @RequestParam YearMonth yearMonth) {
        log.info("수입 내역 조회 - userId: {}, yearMonth: {}", userId, yearMonth);
        return ResponseEntity.ok(transactionService.searchIncomes(userId, yearMonth));
    }
}
