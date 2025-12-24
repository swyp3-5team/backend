package com.moa.entity;

public enum PaymentMethod {
    CARD,
    CASH,
    TRANSFER,
    SIMPLE_PAY,   // 네이버페이, 카카오페이, 토스 등
    ETC;

    public static PaymentMethod from(String payment) {
        for (PaymentMethod pm : PaymentMethod.values()) {
            if (pm.name().equalsIgnoreCase(payment)) {
                return pm;
            }
        }
        return PaymentMethod.ETC;
    }
}
