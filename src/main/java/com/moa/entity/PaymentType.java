package com.moa.entity;

import java.util.Arrays;

public enum PaymentType {
    WEEKLY,
    MONTHLY;

    public static PaymentType from(String paymentType) {
        if (paymentType == null || paymentType.isBlank()) {
            throw new IllegalArgumentException("paymentType is required");
        }
        return Arrays.stream(PaymentType.values())
                .filter(v -> v.name().equalsIgnoreCase(paymentType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid cycle: " + paymentType));
    }
}
