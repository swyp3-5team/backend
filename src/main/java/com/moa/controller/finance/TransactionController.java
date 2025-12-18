package com.moa.controller.finance;

import com.moa.annotation.CurrentUserId;
import com.moa.dto.TransactionCreateRequest;
import com.moa.dto.TransactionResponse;
import com.moa.dto.TransactionUpdateRequest;
import com.moa.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transaction API", description = "지출 기록 조회, 수정, 생성 API")
public class TransactionController {
    private final TransactionService transactionService;

    @Operation(summary = "지출 기록 생성", description = "지출 기록을 생성하는 API")
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestBody TransactionCreateRequest request,
            @CurrentUserId Long userId
    ) {
        return ResponseEntity.ok().body(
                transactionService.createTransaction(request,userId)
        );
    };

    @Operation(summary = "지출 기록 조회", description = "지출 기록을 조회하는 API")
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @CurrentUserId Long userId,
            @RequestParam YearMonth yearMonth
    ){
        return ResponseEntity.ok().body(
                transactionService.getTransactions(userId,yearMonth)
        );
    }

    @Operation(summary = "지출 기록 수정", description = "지출 기록을 수정하는 API")
    @PatchMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @CurrentUserId Long userId,
            @PathVariable Long transactionId,
            @RequestBody TransactionUpdateRequest request
    ){
        return ResponseEntity.ok().body(
                transactionService.updateTransaction(userId,transactionId,request)
        );
    }
}
