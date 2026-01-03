package com.moa.dto;

import com.moa.entity.Transaction;

public record TransactionInfo(
        Long transactionId,
        String name,
        Long amount,
        Long categoryId,
        String categoryName,
        String type
) {
    public static TransactionInfo from(Transaction tr) {
        return new TransactionInfo(
                tr.getId(),
                tr.getName(),
                tr.getAmount(),
                tr.getCategory().getId(),
                tr.getCategory().getName(),
                tr.getCategory().getType().name()
        );
    }
    public static Long parseAmount(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            return null;
        }

        String cleanedAmount = amount.replaceAll("[^0-9]", "");
        return Long.parseLong(cleanedAmount);
    }
}
