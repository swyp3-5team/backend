package com.moa.dto.chat;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 클라이언트 채팅 응답 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {

    private String message;
    private TransactionInfo transactionInfo;

    /**
     * 거래내역 정보 DTO (Nested Class)
     * AI 응답에서 JSON_{...} 형식으로 추출된 정보
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransactionInfo {
        private String pattern;
        private String content;
        private Long amount;
        private String payment;
        private String place;
        private String categoryId;
        private String category;
        private String emotion;
        private LocalDate transactionDate;

        public static Long parseAmount(String amount) {
            if (amount == null || amount.trim().isEmpty()) {
                return null;
            }

            String cleanedAmount = amount.replaceAll("[^0-9]", "");
            return Long.parseLong(cleanedAmount);
        }
    }
}
