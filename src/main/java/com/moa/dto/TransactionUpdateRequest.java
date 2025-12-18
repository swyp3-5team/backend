package com.moa.dto;

import java.time.LocalDateTime;

public record TransactionUpdateRequest(
        Long amount,
        String emotion,
        String transactionMemo,
        String place,
        LocalDateTime transactionDate,
        Long categoryId
) {
}
