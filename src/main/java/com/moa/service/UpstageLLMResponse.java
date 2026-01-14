package com.moa.service;

import java.util.List;

public record UpstageLLMResponse(
        List<Item> items,
        String emotion,
        String payment,
        String comment,
        String place,
        String transactionDate
) {
    public record Item(
            String category,
            String name,
            Long amount
    ) {}
}
