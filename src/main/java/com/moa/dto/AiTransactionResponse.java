package com.moa.dto;

import java.time.LocalDate;
import java.util.List;

public record AiTransactionResponse(
        String place,
        LocalDate transactionDate,
        String payment,
        String paymentMemo,
        Long totalAmount,
        String emotion,
        String type,
        List<TransactionDetailRequest> transactions
) {
}
