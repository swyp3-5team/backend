package com.moa.dto.chat;

import com.moa.dto.TransactionGroupInfo;
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
    private TransactionGroupInfo transactionInfo;
}
