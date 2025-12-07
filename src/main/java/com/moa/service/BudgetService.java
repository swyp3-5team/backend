package com.moa.service;

import com.moa.dto.BudgetResponse;
import com.moa.dto.CreateBudgetRequest;
import com.moa.dto.UpdateBudgetRequest;
import com.moa.entity.Budget;
import com.moa.entity.Category;
import com.moa.entity.User;
import com.moa.repository.BudgetRepository;
import com.moa.repository.CategoryRepository;
import com.moa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public BudgetResponse createBudget(CreateBudgetRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 유저입니다.")
        );

        Category category = categoryRepository.findById(request.cateGoryId()).orElseThrow(
                () -> new IllegalArgumentException("유효하지 않은 카테고리입니다.")
        );
        LocalDate endDate = request.startDate().plusMonths(1);

        Budget budget = Budget.builder()
                .amount(request.amount())
                .memo(request.memo())
                .category(category)
                .user(user)
                .startDate(request.startDate())
                .endDate(endDate)
                .build();
        Budget savedBudget = budgetRepository.save(budget);

        return BudgetResponse.from(savedBudget);
    }

    public List<BudgetResponse> getBudgetsByDate(LocalDate date, Long userId) {
        return budgetRepository.findBudgetsByDate(date,userId)
                .stream()
                .map(BudgetResponse::from)
                .toList();
    }

    @Transactional
    public BudgetResponse updateBudgetAmount(UpdateBudgetRequest request, Long userId) {
        Budget budget = budgetRepository.findByIdAndUser_UserId(request.budgetId(),userId).orElseThrow(
                () -> new IllegalArgumentException("해당 ID의 예산이 존재하지 않습니다.")
        );

        budget.updateAmount(request.amount());
        Budget savedBudget = budgetRepository.save(budget);

        return BudgetResponse.from(savedBudget);
    }
}