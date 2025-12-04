package com.moa.controller;


import com.moa.dto.BudgetResponse;
import com.moa.dto.CreateBudgetRequest;
import com.moa.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/budgets")
@Tag(name = "Budget API", description = "예산 설정, 수정, 조회 API")
public class BudgetController {
    private final BudgetService budgetService;

    @Operation(summary = "예산 생성", description = "특정 카테고리에 대해 예산을 생성한다.")
    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(
            @RequestBody CreateBudgetRequest request,
            @RequestParam Long userId
    ){
        return ResponseEntity.ok(
                budgetService.createBudget(request,userId)
        );
    }

}
