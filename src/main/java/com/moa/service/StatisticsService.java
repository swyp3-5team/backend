package com.moa.service;

import com.moa.dto.response.MonthlyCategoryExpenseResponse;
import com.moa.dto.response.MonthlyEmotionPercentageResponse;
import com.moa.dto.response.MonthlyEmotionStatisticsResponse;
import com.moa.dto.response.MonthlyTotalExpenseResponse;
import com.moa.entity.Transaction;
import com.moa.entity.TransactionEmotion;
import com.moa.entity.TransactionGroup;
import com.moa.repository.TransactionGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class StatisticsService {

    private final TransactionGroupRepository transactionGroupRepository;

    /**
     * 월간 총 지출 금액 조회
     */
    public MonthlyTotalExpenseResponse getMonthlyTotalExpense(Long userId, String date) {
        validateDateFormat(date);
        YearMonth yearMonth = YearMonth.parse(date, DateTimeFormatter.ofPattern("yyyy-MM"));
        int year = yearMonth.getYear();
        int monthValue = yearMonth.getMonthValue();

        List<TransactionGroup> transactionGroups = transactionGroupRepository.findMonthlyExpensesByUserId(userId, year, monthValue);

        long totalExpense = transactionGroups.stream()
                .flatMap(tg -> tg.getTransactions().stream())
                .mapToLong(Transaction::getAmount)
                .sum();

        log.info("사용자 {} 의 {} 총 지출 금액: {}", userId, date, totalExpense);
        return MonthlyTotalExpenseResponse.of(totalExpense);
    }

    /**
     * 월간 카테고리별 지출 금액 조회
     */
    public List<MonthlyCategoryExpenseResponse> getMonthlyCategoryExpense(Long userId, String date) {
        validateDateFormat(date);
        YearMonth yearMonth = YearMonth.parse(date, DateTimeFormatter.ofPattern("yyyy-MM"));
        int year = yearMonth.getYear();
        int monthValue = yearMonth.getMonthValue();

        List<TransactionGroup> transactionGroups = transactionGroupRepository.findMonthlyExpensesByUserId(userId, year, monthValue);

        Map<Long, Long> categoryExpenseMap = transactionGroups.stream()
                .flatMap(tg -> tg.getTransactions().stream())
                .collect(Collectors.groupingBy(
                        t -> t.getCategory().getId(),
                        Collectors.summingLong(Transaction::getAmount)
                ));

        List<MonthlyCategoryExpenseResponse> response = transactionGroups.stream()
                .flatMap(tg -> tg.getTransactions().stream())
                .map(Transaction::getCategory)
                .distinct()
                .filter(category -> categoryExpenseMap.containsKey(category.getId()))
                .map(category -> MonthlyCategoryExpenseResponse.of(category, categoryExpenseMap.get(category.getId())))
                .collect(Collectors.toList());

        log.info("사용자 {} 의 {} 카테고리별 지출: {} 개 카테고리", userId, date, response.size());
        return response;
    }

    /**
     * 월간 감정 통계 조회 (감정별 건수 및 총 금액)
     */
    public List<MonthlyEmotionStatisticsResponse> getMonthlyEmotionStatistics(Long userId, String date) {
        validateDateFormat(date);
        YearMonth yearMonth = YearMonth.parse(date, DateTimeFormatter.ofPattern("yyyy-MM"));
        int year = yearMonth.getYear();
        int monthValue = yearMonth.getMonthValue();

        List<TransactionGroup> transactionGroups = transactionGroupRepository.findMonthlyTransactionGroupsByUserId(userId, year, monthValue);

        Map<TransactionEmotion, List<TransactionGroup>> emotionGroups = transactionGroups.stream()
                .filter(tg -> tg.getEmotion() != null)
                .collect(Collectors.groupingBy(TransactionGroup::getEmotion));

        List<MonthlyEmotionStatisticsResponse> response = emotionGroups.entrySet().stream()
                .map(entry -> new MonthlyEmotionStatisticsResponse(
                        entry.getKey(),
                        (long) entry.getValue().size(),
                        entry.getValue().stream()
                                .flatMap(tg -> tg.getTransactions().stream())
                                .mapToLong(Transaction::getAmount)
                                .sum()
                ))
                .collect(Collectors.toList());

        log.info("사용자 {} 의 {} 감정 통계: {} 개 감정", userId, date, response.size());
        return response;
    }

    /**
     * 월간 감정 비율 조회 (감정별 건수 및 퍼센트)
     */
    public List<MonthlyEmotionPercentageResponse> getMonthlyEmotionPercentage(Long userId, String date) {
        validateDateFormat(date);
        YearMonth yearMonth = YearMonth.parse(date, DateTimeFormatter.ofPattern("yyyy-MM"));
        int year = yearMonth.getYear();
        int monthValue = yearMonth.getMonthValue();

        List<TransactionGroup> transactionGroups = transactionGroupRepository.findMonthlyTransactionGroupsByUserId(userId, year, monthValue);

        List<TransactionGroup> transactionGroupsWithEmotion = transactionGroups.stream()
                .filter(tg -> tg.getEmotion() != null)
                .collect(Collectors.toList());

        long totalCount = transactionGroupsWithEmotion.size();

        if (totalCount == 0) {
            log.info("사용자 {} 의 {} 감정 데이터 없음", userId, date);
            return List.of();
        }

        Map<TransactionEmotion, Long> emotionCounts = transactionGroupsWithEmotion.stream()
                .collect(Collectors.groupingBy(
                        TransactionGroup::getEmotion,
                        Collectors.counting()
                ));

        List<MonthlyEmotionPercentageResponse> response = emotionCounts.entrySet().stream()
                .map(entry -> {
                    double percentage = Math.round((entry.getValue() * 100.0 / totalCount) * 100.0) / 100.0;
                    return new MonthlyEmotionPercentageResponse(
                            entry.getKey(),
                            entry.getValue(),
                            percentage
                    );
                })
                .collect(Collectors.toList());

        log.info("사용자 {} 의 {} 감정 비율: {} 개 감정", userId, date, response.size());
        return response;
    }

    /**
     * 날짜 형식 검증 (yyyy-MM)
     */
    private void validateDateFormat(String date) {
        if (date == null || !date.matches("^\\d{4}-\\d{2}$")) {
            throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다. yyyy-MM 형식으로 입력해주세요. 예: 2025-01");
        }
    }
}
