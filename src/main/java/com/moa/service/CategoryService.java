package com.moa.service;

import com.moa.dto.CategoryResponse;
import com.moa.entity.Category;
import com.moa.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;


    public List<CategoryResponse> getCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(CategoryResponse::from).toList();
    }
}
