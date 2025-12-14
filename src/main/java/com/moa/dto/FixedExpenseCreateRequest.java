package com.moa.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record FixedExpenseCreateRequest(
    Long amount,
    String name,
    String memo,
    @Schema(
            description = "고정 지출 주기",
            example = "weekly",
            allowableValues = {"weekly", "monthly"}
    )
    String paymentType, // Monthly, weekly
    LocalDate initDate,
    Long categoryId
) {
}
