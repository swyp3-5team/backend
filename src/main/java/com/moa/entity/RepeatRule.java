package com.moa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static java.util.Objects.requireNonNull;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Embeddable
public class RepeatRule {

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_TYPE")
    private PaymentType paymentType;

    @Column(name = "WEEKLY_DAY")
    private DayOfWeek weeklyDay;

    @Column(name = "MONTHLY_DAY")
    private Integer monthlyDay;

    @Column(name = "FIXED_DATE")
    private LocalDate fixedDate;

    private RepeatRule(PaymentType type, DayOfWeek weekly, Integer monthly, LocalDate date) {
        this.paymentType = type;
        this.weeklyDay = weekly;
        this.monthlyDay = monthly;
        this.fixedDate = date;
        validate();
    }

    public static RepeatRule weekly(DayOfWeek day) {
        return new RepeatRule(PaymentType.WEEKLY, day, null, null);
    }

    public static RepeatRule monthly(Integer day) {
        return new RepeatRule(PaymentType.MONTHLY, null, day, null);
    }

    public static RepeatRule fixed(LocalDate date) {
        return new RepeatRule(PaymentType.FIXED_DATE, null, null, date);
    }

    public void validate() {
        switch (paymentType) {
            case WEEKLY -> requireNonNull(weeklyDay);
            case MONTHLY -> requireNonNull(monthlyDay);
            case FIXED_DATE -> requireNonNull(fixedDate);
        }
    }
}
