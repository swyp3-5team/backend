package com.moa.dto.response;

import com.moa.entity.TransactionEmotion;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 월간 감정 비율 응답 DTO
 */
public record MonthlyEmotionPercentageResponse(
        @Schema(description = "감정", example = "HAPPY")
        TransactionEmotion emotion,

        @Schema(description = "해당 감정의 거래 건수", example = "15")
        Long count,

        @Schema(description = "전체 대비 비율 (%)", example = "25.5")
        Double percentage
) {
    public static MonthlyEmotionPercentageResponse of(TransactionEmotion emotion, Long count, Double percentage) {
        return new MonthlyEmotionPercentageResponse(emotion, count, percentage);
    }
}
