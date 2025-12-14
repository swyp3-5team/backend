package com.moa.dto;

public record UpdateBudgetRequest(
        Long budgetId,
        Long amount,
        String memo
) {
}
