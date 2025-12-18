package com.moa.dto;

import java.time.LocalDateTime;

public record TransactionCreateRequest(
    Long amount,
    LocalDateTime transactionDate,
    String place,
    String paymentMemo,
    Long categoryId,
    String emotion
) {
}
