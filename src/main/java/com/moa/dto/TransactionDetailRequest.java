package com.moa.dto;

import static com.moa.util.JsonParser.parseAmount;

public record TransactionDetailRequest(
        Long amount,
        String name,
        String categoryName
) {
    public static TransactionDetailRequest fromJson(AiJson.Item item, String category) {
        return new TransactionDetailRequest(
                parseAmount(item.getRaw_amount()),
                item.getName(),
                category
        );
    }
}
