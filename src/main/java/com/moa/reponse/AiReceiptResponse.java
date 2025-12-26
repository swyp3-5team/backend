package com.moa.reponse;

import com.moa.dto.TransactionCreateRequest;

public record AiReceiptResponse(
        String message,
        TransactionCreateRequest request
) {
}
