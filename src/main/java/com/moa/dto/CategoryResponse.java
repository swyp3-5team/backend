package com.moa.dto;

import com.moa.entity.Category;
import com.moa.entity.CategoryType;

public record CategoryResponse(
       Long id,
       CategoryType categoryType,
       String categoryName
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getType(),
                category.getName()
        );
    }
}
