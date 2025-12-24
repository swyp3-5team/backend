package com.moa.dto;

public record TransactionDetailRequest(
        Long amount,
        String name,
        Long categoryId,
        String categoryName
) {
}
