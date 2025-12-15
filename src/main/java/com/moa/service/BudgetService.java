package com.moa.service;

import com.moa.dto.BudgetResponse;
import com.moa.dto.BudgetSuggestionResponse;
import com.moa.dto.CreateBudgetRequest;
import com.moa.dto.UpdateBudgetRequest;
import com.moa.entity.Budget;
import com.moa.entity.Category;
import com.moa.entity.User;
import com.moa.exception.CategoryNotFoundException;
import com.moa.exception.UserNotFoundException;
import com.moa.repository.BudgetRepository;
import com.moa.repository.CategoryRepository;
import com.moa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
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

        Category category = categoryRepository.findById(request.categoryId()).orElseThrow(
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
        return budgetRepository.findBudgetsByDate(date, userId)
                .stream()
                .map(BudgetResponse::from)
                .toList();
    }

    @Transactional
    public BudgetResponse updateBudgetAmount(UpdateBudgetRequest request, Long userId) {
        Budget budget = budgetRepository.findByIdAndUser_UserId(request.budgetId(), userId).orElseThrow(
                () -> new IllegalArgumentException("해당 ID의 예산이 존재하지 않습니다.")
        );

        if (request.amount() != null) {
            budget.updateAmount(request.amount());
        }

        if (request.memo() != null) {
            budget.updateMemo(request.memo());
        }
        Budget savedBudget = budgetRepository.save(budget);

        return BudgetResponse.from(savedBudget);
    }

    @Transactional
    public Long deactivateBudget(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUser_UserId(budgetId, userId).orElseThrow(
                () -> new IllegalArgumentException("존재하지 않는 예산입니다.")
        );

        budget.deactivate();
        Budget savedBudget = budgetRepository.save(budget);

        return savedBudget.getId();
    }

    public List<BudgetSuggestionResponse> getAutoInitBudgets(List<Long> categoryIds, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("존재하지 않는 유저입니다.")
        ); // 추후 LLM 기능 사용할 경우 유저가 아니면 사용불가능하도록 하기 위함
        List<BudgetSuggestionResponse> responses = new ArrayList<>();
        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId).orElseThrow(CategoryNotFoundException::new);

            BudgetSuggestionResponse suggestion = new BudgetSuggestionResponse(category.getId(),category.getName(),100000L); // 추후 자동 추천 로직으로 변경
            responses.add(suggestion);
        }
        return responses;
    }
}