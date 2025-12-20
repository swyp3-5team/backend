package com.moa.service.chat;

import com.moa.dto.chat.ChatResponse.TransactionInfo;
import com.moa.entity.*;
import com.moa.repository.CategoryRepository;
import com.moa.repository.TransactionRepository;
import com.moa.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

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
    private final CategoryRepository categoryRepository;

    @Transactional
    public Long addTransactionInfo(Long userId, TransactionInfo transactionInfo) {
        if (transactionInfo == null) {
            log.info("거래내역 정보가 없어 저장하지 않습니다.");
            return null;
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 금액
        Long amount = transactionInfo.getAmount();

        // 거래일자
        LocalDate transactionDate = transactionInfo.getTransactionDate();

        // 장소
        String place = transactionInfo.getPlace();

        // 결제수단
        String payment = transactionInfo.getPayment();

        // 결제메모
        String paymentMemo = transactionInfo.getContent();

        // 감정
        TransactionEmotion emotion = parseEmotion(transactionInfo.getEmotion());

        // 지출/수입 패턴
        CategoryType categoryType = parseCategoryType(transactionInfo.getPattern());

        // 카테고리
        Category category = findOrCreateCategory(transactionInfo.getCategory(), categoryType);

        Transaction transaction = Transaction.builder()
                .amount(amount)
                .transactionDate(transactionDate)
                .place(place)
                .payment(payment)
                .paymentMemo(paymentMemo)
                .emotion(emotion)
                .category(category)
                .user(user)
                .build();

        transactionRepository.save(transaction);

        log.info("사용자 {}의 거래내역 추가 완료 - 금액: {}, 카테고리: {}, 감정: {}",
                userId, amount, category.getName(), emotion);

        return transaction.getId();
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

    /**
     * 거래내역 조회
     */
    public TransactionInfo getTransaction(Long userId, Long transactionId) {
        Transaction transaction = validTransaction(userId, transactionId);

        return TransactionInfo.builder()
                .amount(transaction.getAmount())
                .transactionDate(transaction.getTransactionDate())
                .place(transaction.getPlace())
                .payment(transaction.getPayment())
                .content(transaction.getPaymentMemo())
                .emotion(transaction.getEmotion().name())
                .category(transaction.getCategory().getName())
                .pattern(transaction.getCategory().getType() == CategoryType.INCOME ? "수입" : "지출")
                .build();
    }

    /**
     * 거래내역 수정
     */
    @Transactional
    public void updateTransaction(Long userId, Long transactionId, TransactionInfo transactionInfo) {
        Transaction transaction = validTransaction(userId, transactionId);

        Category category = null;
        if (transactionInfo.getCategory() != null) {
            CategoryType categoryType = parseCategoryType(transactionInfo.getPattern());
            category = findOrCreateCategory(transactionInfo.getCategory(), categoryType);
        }

        TransactionEmotion emotion = null;
        if (transactionInfo.getEmotion() != null) {
            emotion = parseEmotion(transactionInfo.getEmotion());
        }

        transaction.update(
                transactionInfo.getAmount(),
                transactionInfo.getTransactionDate(),
                transactionInfo.getPlace(),
                transactionInfo.getPayment(),
                transactionInfo.getContent(),  // paymentMemo
                emotion,
                category
        );

        log.info("사용자 {}의 거래내역 {} 수정 완료", userId, transactionId);
    }

    /**
     * 거래내역 삭제
     */
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction transaction = validTransaction(userId, transactionId);

        transactionRepository.delete(transaction);

        log.info("사용자 {}의 거래내역 {} 삭제 완료", userId, transactionId);
    }

    /**
     * 거래내역 유효성 검사 및 소유자 확인
     */
    private Transaction validTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("거래내역을 찾을 수 없습니다."));

        if (!transaction.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("해당 거래내역을 삭제할 권한이 없습니다.");
        }

        return transaction;
    }

    /**
     * Emotion 문자열을 TransactionEmotion으로 변환
     */
    private TransactionEmotion parseEmotion(String emotion) {
        if (emotion == null || emotion.trim().isEmpty()) {
            return TransactionEmotion.NEUTRAL;  // 기본값
        }

        String normalizedEmotion = emotion.trim().toLowerCase();

        // 키워드 매칭
        if (normalizedEmotion.contains("기쁨") || normalizedEmotion.contains("행복") || normalizedEmotion.contains("즐거움") || normalizedEmotion.contains("좋음")) {
            return TransactionEmotion.HAPPY;
        } else if (normalizedEmotion.contains("슬픔") || normalizedEmotion.contains("우울") || normalizedEmotion.contains("아쉬움")) {
            return TransactionEmotion.SADNESS;
        } else if (normalizedEmotion.contains("스트레스") || normalizedEmotion.contains("해소") || normalizedEmotion.contains("풀림") || normalizedEmotion.contains("힐링")) {
            return TransactionEmotion.STRESS_RELIEF;
        } else if (normalizedEmotion.contains("보상") || normalizedEmotion.contains("기분전환") || normalizedEmotion.contains("선물")) {
            return TransactionEmotion.REWARD;
        } else if (normalizedEmotion.contains("충동") || normalizedEmotion.contains("즉흥") || normalizedEmotion.contains("갑작스러움")) {
            return TransactionEmotion.IMPULSE;
        } else if (normalizedEmotion.contains("계획") || normalizedEmotion.contains("목적") || normalizedEmotion.contains("예정")) {
            return TransactionEmotion.PLANNED;
        } else if (normalizedEmotion.contains("후회") || normalizedEmotion.contains("아까움") || normalizedEmotion.contains("낭비")) {
            return TransactionEmotion.REGRET;
        } else if (normalizedEmotion.contains("만족") || normalizedEmotion.contains("충족") || normalizedEmotion.contains("행복감")) {
            return TransactionEmotion.SATISFACTION;
        } else {
            return TransactionEmotion.NEUTRAL;
        }
    }
}
