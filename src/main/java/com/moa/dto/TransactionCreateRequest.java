package com.moa.dto;

import java.time.LocalDate;
import java.util.List;

public record TransactionCreateRequest(
    String place,
    LocalDate transactionDate,
    String payment,
    String paymentMemo,
    String emotion,
    String type,
    List<TransactionDetailRequest> transactions
) {
}
