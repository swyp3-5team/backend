package com.moa.dto;

import com.moa.entity.Transaction;

import java.time.LocalDateTime;

public record TransactionResponse(
        Long transactionId,
        Long amount,
        String place,
        String paymentMemo,
        Long categoryId,
        String emotion,
        LocalDateTime transactionDate
) {
    public static TransactionResponse from(Transaction savedTransaction) {
        return new TransactionResponse(
                savedTransaction.getId(),
                savedTransaction.getAmount(),
                savedTransaction.getPlace(),
                savedTransaction.getPaymentMemo(),
                savedTransaction.getCategory().getId(),
                savedTransaction.getEmotion().name(),
                savedTransaction.getTransactionDate()
        );
    }
}
