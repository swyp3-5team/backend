package com.moa.dto;

import com.moa.entity.FixedExpense;

import java.time.LocalDate;

public record FixedExpenseResponse(
        Long id,
        Long amount,
        String name,
        String memo,
        String paymentType,
        LocalDate nextDate
) {
    public static FixedExpenseResponse from(FixedExpense savedFixedExpense,LocalDate nextDate) {
        return new FixedExpenseResponse(
                savedFixedExpense.getId(),
                savedFixedExpense.getAmount(),
                savedFixedExpense.getName(),
                savedFixedExpense.getMemo(),
                savedFixedExpense.getRepeatRule().getPaymentType().name(),
                nextDate
        );
    }
}
