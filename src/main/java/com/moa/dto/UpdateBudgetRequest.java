package com.moa.dto;

public record UpdateBudgetRequest(
        Long amount,
        String memo
) {
}
