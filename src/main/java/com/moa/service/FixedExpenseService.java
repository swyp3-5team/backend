package com.moa.service;

import com.moa.dto.FixedExpenseCreateRequest;
import com.moa.dto.FixedExpenseResponse;
import com.moa.entity.*;
import com.moa.exception.CategoryNotFoundException;
import com.moa.exception.UserNotFoundException;
import com.moa.repository.CategoryRepository;
import com.moa.repository.FixedExpenseRepository;
import com.moa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FixedExpenseService {
    private final FixedExpenseRepository fixedExpenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public FixedExpenseResponse createFixedExpense(Long userId, FixedExpenseCreateRequest request) {
        PaymentType paymentType = PaymentType.from(request.paymentType());
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(CategoryNotFoundException::new);
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException(userId + "존재하지 않는 유저입니다.")
        );

        RepeatRule repeatRule;
        if (paymentType == PaymentType.WEEKLY) {
            repeatRule = RepeatRule.weekly(request.initDate().getDayOfWeek());
        } else {
            repeatRule = RepeatRule.monthly(request.initDate().getDayOfMonth());
        }
        FixedExpense fixedExpense = FixedExpense
                .builder()
                .name(request.name())
                .amount(request.amount())
                .memo(request.memo())
                .initDate(request.initDate())
                .repeatRule(repeatRule)
                .category(category)
                .user(user)
                .isActive(true)
                .build();

        FixedExpense savedFixedExpense = fixedExpenseRepository.save(fixedExpense);

        LocalDate nextDate = savedFixedExpense.nextPaymentDate(request.initDate());

        return FixedExpenseResponse.from(savedFixedExpense, nextDate);
    }

    public List<FixedExpenseResponse> getFixedExpenses(Long userId) {
        List<FixedExpense> fixedExpenses = fixedExpenseRepository.findByUser_UserIdAndIsActiveTrue(userId);

        return fixedExpenses.stream()
                .map(fixedExpense -> FixedExpenseResponse.from(fixedExpense, fixedExpense.nextPaymentDate(LocalDate.now())))
                .toList();
    }
}
