package com.moa.service.chat;

import com.moa.dto.TransactionCreateRequest;
import com.moa.dto.TransactionDetailRequest;
import com.moa.dto.TransactionGroupInfo;
import com.moa.dto.TransactionInfo;
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
import java.util.List;
import java.util.Optional;

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
        LocalDate transactionDate = request.transactionDate();

        // 장소
        String place = request.place();

        // 결제수단
        String payment = request.payment();

        // 결제메모
        String paymentMemo = request.paymentMemo();

        // 감정
        TransactionEmotion emotion = parseEmotion(request.emotion());

        // 지출/수입 패턴
        CategoryType categoryType = parseCategoryType(request.type());

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
                    Category category = findOrCreateCategory(tr.categoryName(), categoryType);
                    return Transaction.builder()
                            .name(tr.name())
                            .amount(tr.amount())
                            .category(category)
                            .transactionGroup(transactionGroup)
                            .build();
                }
        ).toList();

        transactionGroupRepository.save(transactionGroup);
        transactionRepository.saveAll(transactionList);

        log.info("사용자 {}의 거래내역 추가 완료 - 감정: {}",
                userId,emotion);

        return transactionGroup.getId();
    }

    /*
    * 2025-12 형태의 날짜 입력을 기반으로 해당 월의 기록을 조회
    * */
    @Transactional(readOnly=true)
    public List<TransactionGroupInfo> getTransactionsByYearMonth(Long userId, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<TransactionGroup> trGroupList = transactionGroupRepository.findByUser_UserIdAndTransactionDateBetween(userId, start, end);

        if(trGroupList.isEmpty()){
            return null;
        }

        return trGroupList.stream().map(
                trGroup -> TransactionGroupInfo.from(
                        trGroup,
                        trGroup.getTransactions().stream().map(
                                com.moa.dto.TransactionInfo::from
                        ).toList()))
                .toList();
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
        TransactionGroup transactionGroup = transactionGroupRepository.findByIdAndUser_UserId(transactionId,userId).orElseThrow(
                ()-> new RuntimeException("거래 기록을 찾을 수 없습니다.")
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
        TransactionGroup transactionGroup = transactionGroupRepository.findByIdAndUser_UserId(userId,transactionGroupId).orElseThrow(
                () -> new RuntimeException("거래 내역이 없습니다.")
        );
        for(TransactionInfo transactionInfo : transactionGroupInfo.transactionInfoList()) {
            if (transactionInfo == null) {
                return;
            }
            Category category = findOrCreateCategory(transactionInfo.categoryName(), CategoryType.EXPENSE);
            transactionRepository.findById(transactionInfo.transactionId()).ifPresent(
                    tr -> {
                        tr.update(
                                transactionInfo.name(),
                                transactionInfo.amount(),
                                category
                        );
                    }
            );
        }

        log.info("사용자 {}의 거래내역 {} 수정 완료", userId, transactionGroupId);
    }

    /**
     * 거래내역 삭제
     */
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        TransactionGroup transactionGroup = transactionGroupRepository.findByIdAndUser_UserId(userId,transactionId).orElseThrow(
                () -> new RuntimeException("존재하지 않는 지출 기록입니다.")
        );

        transactionGroupRepository.delete(transactionGroup);

        log.info("사용자 {}의 거래내역 {} 삭제 완료", userId, transactionId);
    }

    /**
     * Category 찾기 또는 기타로 설정
     */
    private Category findOrCreateCategory(String content, CategoryType type) {
        // content가 null이거나 빈 문자열이면 "기타"로 설정
        final String categoryName = (content == null || content.trim().isEmpty()) ? "기타" : content;

        // 기존 카테고리 찾기 (이름과 타입으로)
        Optional<Category> category = categoryRepository.findByNameAndType(categoryName, type);

        if (category.isPresent()) {
            return category.get();
        }

        // 없으면 "기타" 카테고리로 대체
        return categoryRepository.findByNameAndType("기타", type).get();
    }
}
