package com.moa.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 월간 총 지출 금액 응답 DTO
 */
public record MonthlyTotalExpenseResponse(
        @Schema(description = "총 금액" , example = "100000")
        Long totalExpense
) {
    public static MonthlyTotalExpenseResponse of(Long totalExpense) {
        return new MonthlyTotalExpenseResponse(totalExpense);
    }
}
