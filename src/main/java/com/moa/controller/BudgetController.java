package com.moa.controller;


import com.moa.annotation.CurrentUserId;
import com.moa.dto.BudgetResponse;
import com.moa.dto.CreateBudgetRequest;
import com.moa.dto.UpdateBudgetRequest;
import com.moa.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    ){
        return ResponseEntity.ok(
                budgetService.createBudget(request,userId)
        );
    }

    @Operation(summary = "예산 조회", description = "연-월별 예산 조회")
    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgetsByDate(
            @RequestParam LocalDate date,
            @CurrentUserId Long userId
            ){
        return ResponseEntity.ok(
                budgetService.getBudgetsByDate(date, userId)
        );
    }

    @Operation(summary = "예산 수정", description = "예산 금액을 수정")
    @PutMapping
    public ResponseEntity<BudgetResponse> updateBudgetAmount(
            @RequestBody UpdateBudgetRequest request,
            @CurrentUserId Long userId
    ){
        return ResponseEntity.ok(
                budgetService.updateBudgetAmount(request,userId)
        );
    }

}
