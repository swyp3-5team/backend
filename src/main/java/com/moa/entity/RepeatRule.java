package com.moa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;

import static java.util.Objects.requireNonNull;

@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Embeddable
@Getter
public class RepeatRule {

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_TYPE")
    private PaymentType paymentType;

    @Column(name = "WEEKLY_DAY")
    private DayOfWeek weeklyDay;

    @Column(name = "MONTHLY_DAY")
    private Integer monthlyDay;

    private RepeatRule(PaymentType type, DayOfWeek weekly, Integer monthly, LocalDate date) {
        this.paymentType = type;
        this.weeklyDay = weekly;
        this.monthlyDay = monthly;
        validate();
    }

    public static RepeatRule weekly(DayOfWeek day) {
        return new RepeatRule(PaymentType.WEEKLY, day, null, null);
    }

    public static RepeatRule monthly(Integer day) {
        return new RepeatRule(PaymentType.MONTHLY, null, day, null);
    }

    public void validate() {
        switch (paymentType) {
            case WEEKLY -> requireNonNull(weeklyDay);
            case MONTHLY -> requireNonNull(monthlyDay);
        }
    }

    public LocalDate calculateNextDate(LocalDate initDate, LocalDate today) {
        if(today.isBefore(initDate)) {
            return initDate;
        }

        return switch (paymentType) {
            case WEEKLY -> calculateWeekly(today);
            case MONTHLY -> calculateMonthly(today);
        };
    }

    private LocalDate calculateMonthly(LocalDate today) {
        YearMonth currentYearMonth = YearMonth.from(today);

        LocalDate nextDate = currentYearMonth.atDay(monthlyDay);

        if(nextDate.isBefore(today)) {
            nextDate = currentYearMonth.plusMonths(1).atDay(monthlyDay);
        }
        return nextDate;
    }

    private LocalDate calculateWeekly(LocalDate today) {
        LocalDate nextDate = today.with(TemporalAdjusters.nextOrSame(weeklyDay));

        return nextDate;
    }
}
