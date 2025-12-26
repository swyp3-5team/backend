package com.moa.dto;

import com.moa.entity.Category;

import static com.moa.util.JsonParser.parseAmount;

public record TransactionDetailRequest(
        Long amount,
        String name,
        Long categoryId,
        String categoryName
) {
    public static TransactionDetailRequest fromJson(AiJson.Item item, Category category) {
        return new TransactionDetailRequest(
                parseAmount(item.getRaw_amount()),
                item.getName(),
                category.getId(),
                category.getName()
        );

    }
}
