package com.moa.repository;

import com.moa.entity.TransactionGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionGroupRepository extends JpaRepository<TransactionGroup, Long> {
    List<TransactionGroup> findByUser_UserIdAndTransactionDateBetween(Long userId, LocalDate start, LocalDate end);

    Optional<TransactionGroup> findByIdAndUser_UserId(Long userId, Long transactionGroupId);
}
