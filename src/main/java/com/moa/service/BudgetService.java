package com.moa.service;

import com.moa.dto.BudgetResponse;
import com.moa.dto.CreateBudgetRequest;
import com.moa.entity.Budget;
import com.moa.entity.Category;
import com.moa.entity.User;
import com.moa.repository.BudgetRepository;
import com.moa.repository.CategoryRepository;
import com.moa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public BudgetResponse createBudget(CreateBudgetRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 유저입니다.")
        );

        Category category = categoryRepository.findById(request.cateGoryId()).orElseThrow(
                () -> new IllegalArgumentException("유효하지 않은 카테고리입니다.")
        );

        Budget budget = Budget.builder()
                .amount(request.amount())
                .memo(request.memo())
                .category(category)
                .user(user)
                .build();
        Budget savedBudget = budgetRepository.save(budget);

        return BudgetResponse.from(savedBudget);
    }
}
