package com.moa.reponse;

import com.moa.dto.AiTransactionResponse;

public record AiReceiptResponse(
        String message,
        AiTransactionResponse request
) {
}
