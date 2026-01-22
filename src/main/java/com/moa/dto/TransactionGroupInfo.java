package com.moa.dto;

import com.moa.entity.TransactionGroup;

import java.time.LocalDate;
import java.util.List;

public record TransactionGroupInfo(
        Long trGroupId,
        LocalDate transactionDate,
        Long totalAmount,
        String place,
        String payment,
        String paymentMemo,
        String emotion,
        List<TransactionInfo> transactionInfoList

) {
    public static TransactionGroupInfo from(TransactionGroup transactionGroup, List<TransactionInfo> transactionInfos) {
        return new TransactionGroupInfo(
                transactionGroup.getId(),
                transactionGroup.getTransactionDate(),
                transactionInfos.stream().mapToLong(
                        TransactionInfo::amount
                ).sum(),
                transactionGroup.getPlace(),
                transactionGroup.getPayment().name(),
                transactionGroup.getPaymentMemo(),
                transactionGroup.getEmotion().name(),
                transactionInfos
        );
    }

}
