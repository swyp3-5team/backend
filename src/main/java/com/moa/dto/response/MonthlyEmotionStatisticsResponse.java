package com.moa.dto.response;

import com.moa.entity.TransactionEmotion;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 월간 감정 통계 응답 DTO
 */
public record MonthlyEmotionStatisticsResponse(
        @Schema(description = "감정", example = "HAPPY")
        TransactionEmotion emotion,

        @Schema(description = "해당 감정의 거래 건수", example = "15")
        Long count,

        @Schema(description = "해당 감정의 총 지출 금액", example = "500000")
        Long totalAmount
) {
    public static MonthlyEmotionStatisticsResponse of(TransactionEmotion emotion, Long count, Long totalAmount) {
        return new MonthlyEmotionStatisticsResponse(emotion, count, totalAmount);
    }
}
