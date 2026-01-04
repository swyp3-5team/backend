package com.moa.repository;

import com.moa.entity.PaymentMethod;
import com.moa.entity.TransactionEmotion;
import com.moa.entity.TransactionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionGroupRepository extends JpaRepository<TransactionGroup, Long> {
    List<TransactionGroup> findByUser_UserIdAndTransactionDateBetween(Long userId, LocalDate start, LocalDate end);

    Optional<TransactionGroup> findByIdAndUser_UserId(Long transactionGroupId, Long userId);

    /**
     * 특정 사용자의 특정 월 거래그룹 조회 (지출만)
     */
    @Query("SELECT DISTINCT tg FROM TransactionGroup tg " +
           "JOIN FETCH tg.transactions t " +
           "JOIN FETCH t.category c " +
           "WHERE tg.user.id = :userId " +
           "AND YEAR(tg.transactionDate) = :year " +
           "AND MONTH(tg.transactionDate) = :month " +
           "AND c.type = com.moa.entity.CategoryType.EXPENSE")
    List<TransactionGroup> findMonthlyExpensesByUserId(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month
    );

    /**
     * 특정 사용자의 특정 월 모든 거래그룹 조회 (감정 통계용)
     */
    @Query("SELECT DISTINCT tg FROM TransactionGroup tg " +
           "JOIN FETCH tg.transactions t " +
           "WHERE tg.user.id = :userId " +
           "AND YEAR(tg.transactionDate) = :year " +
           "AND MONTH(tg.transactionDate) = :month")
    List<TransactionGroup> findMonthlyTransactionGroupsByUserId(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month
    );

    /**
     * 거래내역 검색 (월 단위 + paymentMemo, payment, emotion, categoryId 필터링)
     */
    @Query("SELECT DISTINCT tg FROM TransactionGroup tg " +
           "LEFT JOIN FETCH tg.transactions t " +
           "LEFT JOIN FETCH t.category c " +
           "WHERE tg.user.id = :userId " +
           "AND tg.isDeleted = false " +
           "AND YEAR(tg.transactionDate) = :year " +
           "AND MONTH(tg.transactionDate) = :month " +
           "AND (:paymentMemo IS NULL OR :paymentMemo = '' OR tg.paymentMemo LIKE CONCAT('%', CAST(:paymentMemo AS string), '%')) " +
           "AND (:payment IS NULL OR tg.payment = :payment) " +
           "AND (:emotion IS NULL OR tg.emotion = :emotion) " +
           "AND (:categoryId IS NULL OR c.id = :categoryId) " +
           "ORDER BY tg.transactionDate DESC")
    List<TransactionGroup> searchTransactions(
            @Param("userId") Long userId,
            @Param("year") int year,
            @Param("month") int month,
            @Param("paymentMemo") String paymentMemo,
            @Param("payment") PaymentMethod payment,
            @Param("emotion") TransactionEmotion emotion,
            @Param("categoryId") Long categoryId
    );
}
