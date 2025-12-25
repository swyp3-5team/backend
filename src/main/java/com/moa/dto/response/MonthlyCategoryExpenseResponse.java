package com.moa.dto.response;

import com.moa.entity.Category;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 월간 카테고리별 지출 금액 응답 DTO
 */
public record MonthlyCategoryExpenseResponse(
        @Schema(description = "카테고리 ID" , example = "1")
       Long categoryId,

        @Schema(description = "카테고리 이름" , example = "식비")
       String categoryName,

        @Schema(description = "카테고리별 총 지출 금액" , example = "10000")
       Long totalAmount
) {
    public static MonthlyCategoryExpenseResponse of(Category category, Long totalAmount) {
        return new MonthlyCategoryExpenseResponse(
                category.getId(),
                category.getName(),
                totalAmount
        );
    }
}
