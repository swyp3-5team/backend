package com.moa.dto;

import java.time.LocalDate;

public record TransactionUpdateRequest(
        Long amount,
        String emotion,
        String transactionMemo,
        String place,
        LocalDate transactionDate,
        Long categoryId
) {
}
