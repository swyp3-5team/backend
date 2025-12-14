package com.moa.controller.finance;

import com.moa.annotation.CurrentUserId;
import com.moa.dto.FixedExpenseCreateRequest;
import com.moa.dto.FixedExpenseResponse;
import com.moa.service.FixedExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fixed-expenses")
@Tag(name = "Fixed-Expense API", description = "고정 지출 수정,조회,생성 API")
public class FixedExpenseController {
    private final FixedExpenseService fixedExpenseService;

    @Operation(summary = "고정 지출 생성", description = "고정 지출 생성")
    @PostMapping
    public ResponseEntity<FixedExpenseResponse> createFixedExpense(
            @RequestBody FixedExpenseCreateRequest request,
            @CurrentUserId Long userId
    ) {
        return ResponseEntity
                .ok()
                .body(fixedExpenseService.createFixedExpense(userId, request));
    };

    @Operation(summary = "고정 지출 조회", description = "오늘 기준 고정 지출 & 다음 결제일 조회")
    @GetMapping
    public ResponseEntity<List<FixedExpenseResponse>> getFixedExpenses(
            @CurrentUserId Long userId
    ) {
        return ResponseEntity
                .ok()
                .body(fixedExpenseService.getFixedExpenses(userId));
    };
}
