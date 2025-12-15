package com.moa.controller.finance;


import com.moa.annotation.CurrentUserId;
import com.moa.dto.*;
import com.moa.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/budgets")
@Tag(name = "Budget API", description = "예산 설정, 수정, 조회 API")
public class BudgetController {
    private final BudgetService budgetService;

    @Operation(summary = "예산 생성", description = "특정 카테고리에 대해 예산을 생성")
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @RequestBody CreateBudgetRequest request,
            @CurrentUserId Long userId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(budgetService.createBudget(request, userId));
    }

    @Operation(summary = "예산 조회", description = "연-월별 예산 조회")
    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgetsByDate(
            @RequestParam LocalDate date,
            @CurrentUserId Long userId
    ) {
        return ResponseEntity.ok(
                budgetService.getBudgetsByDate(date, userId)
        );
    }

    @Operation(summary = "예산 수정", description = "예산 금액, 메모 등을 수정")
    @PatchMapping
    public ResponseEntity<BudgetResponse> updateBudgetAmount(
            @RequestBody UpdateBudgetRequest request,
            @CurrentUserId Long userId
    ) {
        return ResponseEntity.ok(
                budgetService.updateBudgetAmount(request, userId)
        );
    }

    @Operation(summary = "예산 비활성화", description = "ID에 해당하는 예산 비활성화")
    @PostMapping("/{budgetId}/deactivate")
    public ResponseEntity<Long> deactivateBudget(
            @CurrentUserId Long userId,
            @PathVariable Long budgetId
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                budgetService.deactivateBudget(userId, budgetId)
        );
    }

    @Operation(summary = "자동 예산 설정", description = "예산을 지정해주는 기능")
    @PostMapping("/auto")
    public ResponseEntity<List<BudgetSuggestionResponse>> getAutoInitBudgets(
            @RequestBody AutoInitRequest categoryIds,
            @CurrentUserId Long userId
    ) {
        return ResponseEntity.ok(
                budgetService.getAutoInitBudgets(categoryIds.categoryIds(), userId)
        );
    }
}
