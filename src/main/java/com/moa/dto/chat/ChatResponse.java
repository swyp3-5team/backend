package com.moa.dto.chat;

import com.moa.entity.PaymentMethod;
import com.moa.entity.TransactionEmotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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
        private Long amount; // 금액
        private Long categoryId;
        private String category;
        /** 지출/입금 */
        private String pattern;

        /** 내용 */
        private String content;

        /** 결제일 */
        private LocalDate transactionDate;

        /** 결제처 */
        private String place;

        /** 결제 수단(ex. 현금, 카드, 계좌 이제 등) */
        private PaymentMethod paymentMethod;

        /** 감정 */
        private TransactionEmotion emotion;

        /** 영수증 품목 목록 */
        private List<TransactionInfo> items;

        public static Long parseAmount(String amount) {
            if (amount == null || amount.trim().isEmpty()) {
                return null;
            }

            String cleanedAmount = amount.replaceAll("[^0-9]", "");
            return Long.parseLong(cleanedAmount);
        }
    }
}
