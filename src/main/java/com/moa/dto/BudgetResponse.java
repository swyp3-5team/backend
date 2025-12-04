package com.moa.dto;

import com.moa.entity.Budget;
import io.swagger.v3.oas.annotations.media.Schema;

public record BudgetResponse(
        @Schema(description = "예산 ID" , example = "1")
        Long id,

        @Schema(description = "예산 금액", example = "250000")
        Long amount,

        @Schema(description = "메모" ,  example = "식비 예산")
        String memo,

        @Schema(description = "카테고리 ID" , example = "3")
        Long categoryId,

        @Schema(description = "유저 ID")
        Long userId
) {
    public static BudgetResponse from(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getAmount(),
                budget.getMemo(),
                budget.getCategory().getId(),
                budget.getUser().getUserId()
        );
    }
}
