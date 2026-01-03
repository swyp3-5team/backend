package com.moa.dto.chat;

import com.moa.dto.AiTransactionResponse;
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
public class ReceiptResponse {

    private String message;
    private AiTransactionResponse transactionInfo;
}
