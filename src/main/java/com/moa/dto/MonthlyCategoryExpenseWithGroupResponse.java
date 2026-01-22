package com.moa.dto;

import com.moa.entity.PaymentMethod;
import com.moa.entity.TransactionEmotion;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class MonthlyCategoryExpenseWithGroupResponse {
    private final String name;
    private final Long amount;
    private final LocalDate date;
    private final String emotion;
    private final String payment;

    public MonthlyCategoryExpenseWithGroupResponse(String name, Long amount, LocalDate date, TransactionEmotion emotion, PaymentMethod payment) {
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.emotion = emotion.toString();
        this.payment = payment.toString();
    }
}