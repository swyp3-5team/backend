package com.moa.controller.finance;

import com.moa.dto.CategoryResponse;
import com.moa.service.CategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@Tag(name = "CATEGORY API", description = "카테고리 조회 API")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity
                .ok()
                .body(categoryService.getCategories());
    }
}
