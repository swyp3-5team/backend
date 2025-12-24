package com.moa.dto;

import com.moa.entity.Transaction;

public record TransactionInfo(
        Long transactionId,
        String name,
        Long amount,
        Long categoryId,
        String categoryName
) {
    public static TransactionInfo from(Transaction tr) {
        return new TransactionInfo(
                tr.getId(),
                tr.getName(),
                tr.getAmount(),
                tr.getCategory().getId(),
                tr.getCategory().getName()
        );
    }
}
