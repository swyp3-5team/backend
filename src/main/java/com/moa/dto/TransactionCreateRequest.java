package com.moa.dto;

import java.time.LocalDate;

public record TransactionCreateRequest(
    Long amount,
    LocalDate transactionDate,
    String place,
    String paymentMemo,
    Long categoryId,
    String emotion
) {
}
