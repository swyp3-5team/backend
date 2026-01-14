package com.moa.service.chat;

import com.moa.dto.*;
import com.moa.entity.*;
import com.moa.repository.CategoryRepository;
import com.moa.repository.TransactionGroupRepository;
import com.moa.repository.TransactionRepository;
import com.moa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.moa.entity.TransactionEmotion.parseEmotion;

/**
 * 거래내역 비즈니스 로직 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionGroupRepository transactionGroupRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Long addTransactionInfo(Long userId, TransactionCreateRequest request) {
        if (request == null || request.transactions().isEmpty()) {
            log.info("거래 상세내역은 최소 1개 이상이어야 합니다.");
            return null;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 거래일자
        LocalDate transactionDate = LocalDate.now();
        if (request.transactionDate() != null) {
            transactionDate = request.transactionDate();
        }

        // 장소
        String place = request.place();

        // 결제수단
        String payment = request.payment();

        // 결제메모
        String paymentMemo = request.paymentMemo();

        // 감정
        TransactionEmotion emotion = parseEmotion(request.emotion());

        TransactionGroup transactionGroup = TransactionGroup.builder()
                .user(user)
                .transactionDate(transactionDate)
                .place(place)
                .payment(PaymentMethod.from(payment))
                .paymentMemo(paymentMemo)
                .emotion(emotion)
                .build();

        // 그룹에 포함된 세부내역으로 transaction 생성
        List<TransactionDetailRequest> transactions = request.transactions();

        // 묶음 내부를 순회하며 Transaction 생성
        List<Transaction> transactionList = transactions.stream().map(
                (tr) -> {
                    Category category = findCategory(tr.categoryName());

                    Transaction transaction =  Transaction.builder()
                            .name(tr.name())
                            .amount(tr.amount())
                            .category(category)
                            .transactionGroup(transactionGroup)
                            .build();
                    transactionGroup.addTransaction(transaction);
                    return transaction;
                }
        ).toList();


        transactionGroupRepository.save(transactionGroup);
        transactionRepository.saveAll(transactionList);

        log.info("사용자 {}의 거래내역 추가 완료 - 감정: {}",
                userId, emotion);

        return transactionGroup.getId();
    }


    /**
     * Pattern 문자열을 CategoryType으로 변환
     */
    private CategoryType parseCategoryType(String pattern) {
        if (pattern == null) {
            return CategoryType.EXPENSE;  // 기본값: 지출
        }

        String normalizedPattern = pattern.trim().toLowerCase();
        if (normalizedPattern.contains("수입") || normalizedPattern.contains("입금") || normalizedPattern.contains("저축")) {
            return CategoryType.INCOME;
        } else {
            return CategoryType.EXPENSE;
        }
    }

    /**
     * 거래내역 조회
     */
    public TransactionGroupInfo getTransaction(Long userId, Long transactionId) {
        TransactionGroup transactionGroup = transactionGroupRepository.findByIdAndUser_UserId(transactionId, userId).orElseThrow(
                () -> new RuntimeException("거래 기록을 찾을 수 없습니다.")
        );

        return TransactionGroupInfo.from(
                transactionGroup,
                transactionGroup.getTransactions().stream().map(
                        TransactionInfo::from
                ).toList()
        );
    }

    /**
     * 거래내역 수정
     */
    @Transactional
    public void updateTransaction(Long userId, Long transactionGroupId, TransactionGroupInfo transactionGroupInfo) {
        TransactionGroup transactionGroup = transactionGroupRepository.findByIdAndUser_UserId(transactionGroupId, userId).orElseThrow(
                () -> new RuntimeException("거래 내역이 없습니다.")
        );
        transactionGroup.update(transactionGroupInfo);
        Set<Long> requestIds = transactionGroupInfo.transactionInfoList().stream()
                .map(TransactionInfo::transactionId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        transactionGroup.getTransactions().removeIf(
                transaction -> !requestIds.contains(transaction.getId())
        );

        Map<Long, TransactionInfo> infoMap =
                transactionGroupInfo.transactionInfoList().stream()
                        .filter(info -> info.transactionId() != null)
                        .collect(Collectors.toMap(
                                TransactionInfo::transactionId,
                                Function.identity()
                        ));


        for(Transaction transaction : transactionGroup.getTransactions()){
            TransactionInfo info = infoMap.get(transaction.getId());
            if (info != null) {
                Category category = findCategory(info.categoryName());
                transaction.update(info.name(), info.amount(), category);
            }
        }

        transactionGroupInfo.transactionInfoList().stream()
                .filter(info -> info.transactionId() == null)
                .forEach(info -> {
                    Category category = findCategory(info.categoryName());
                    Transaction newTransaction = Transaction.builder()
                            .name(info.name())
                            .amount(info.amount())
                            .category(category)
                            .transactionGroup(transactionGroup)
                            .build();
                    transactionGroup.addTransaction(newTransaction);
                });
        log.info("사용자 {}의 거래내역 {} 수정 완료", userId, transactionGroupId);
    }

    /**
     * 거래내역 삭제
     */
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        TransactionGroup transactionGroup = transactionGroupRepository.findByIdAndUser_UserId(transactionId, userId).orElseThrow(
                () -> new RuntimeException("존재하지 않는 지출 기록입니다.")
        );

        transactionGroupRepository.delete(transactionGroup);

        log.info("사용자 {}의 거래내역 {} 삭제 완료", userId, transactionId);
    }

    /**
     * 거래내역 검색
     */
    public List<TransactionGroupInfo> searchTransactions(
            Long userId,
            YearMonth yearMonth,
            String paymentMemo,
            String payment,
            String emotion,
            Long categoryId
    ) {
        // String을 enum으로 변환
        PaymentMethod paymentMethod = null;
        if (payment != null && !payment.trim().isEmpty()) {
            try {
                paymentMethod = PaymentMethod.valueOf(payment.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 payment 값: {}", payment);
            }
        }

        TransactionEmotion transactionEmotion = null;
        if (emotion != null && !emotion.trim().isEmpty()) {
            try {
                transactionEmotion = TransactionEmotion.valueOf(emotion.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 emotion 값: {}", emotion);
            }
        }

        int year = yearMonth.getYear();
        int month = yearMonth.getMonthValue();

        List<TransactionGroup> results = transactionGroupRepository.searchTransactions(
                userId, year, month, paymentMemo, paymentMethod, transactionEmotion, categoryId
        );

        log.info("거래내역 검색 완료 - userId: {}, yearMonth: {}, 검색 결과: {}건", userId, yearMonth, results.size());

        return results.stream()
                .map(tg -> TransactionGroupInfo.from(
                        tg,
                        tg.getTransactions().stream().map(TransactionInfo::from).toList()
                ))
                .toList();
    }

    /**
     * Category 찾기 또는 기타로 설정
     */
    private Category findCategory(String content) {
        // content가 null이거나 빈 문자열이면 "기타"로 설정
        final String categoryName = (content == null || content.trim().isEmpty()) ? "기타" : content;

        // 기존 카테고리 찾기 (이름과 타입으로)
        Optional<Category> category = categoryRepository.findByName(categoryName);

        if (category.isPresent()) {
            return category.get();
        }

        // 없으면 "기타" 카테고리로 대체
        return categoryRepository.findByName("기타").get();
    }
}
