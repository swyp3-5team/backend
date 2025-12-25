package com.moa.repository;

import com.moa.entity.TransactionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionGroupRepository extends JpaRepository<TransactionGroup, Long> {
    List<TransactionGroup> findByUser_UserIdAndTransactionDateBetween(Long userId, LocalDate start, LocalDate end);

    Optional<TransactionGroup> findByIdAndUser_UserId(Long userId, Long transactionGroupId);

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
}
