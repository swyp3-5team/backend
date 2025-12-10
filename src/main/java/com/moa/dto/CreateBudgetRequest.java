package com.moa.dto;


import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record CreateBudgetRequest(
        @Schema(description = "예산 금액", example = "200000")
        Long amount,

        @Schema(description = "예산 메모", example = "식비 예산")
        String memo,

        @Schema(description = "카테고리 ID", example = "3")
        Long cateGoryId,

        @Schema(description = "시작 날짜", example = "2025-12-07")
        LocalDate startDate
) {
}
