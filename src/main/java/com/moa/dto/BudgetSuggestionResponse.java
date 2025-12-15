package com.moa.dto;

public record BudgetSuggestionResponse(
        Long categoryId,
        String categoryName,
        Long amount
) { }
