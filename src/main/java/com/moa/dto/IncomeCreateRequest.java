package com.moa.dto;

import java.time.LocalDate;
import java.util.List;

public record IncomeCreateRequest(
        String place,
        LocalDate transactionDate,
        List<TransactionDetailRequest> incomes
) {
}
