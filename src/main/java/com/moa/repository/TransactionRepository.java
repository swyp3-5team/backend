package com.moa.repository;

import com.moa.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser_UserIdAndTransactionDateBetween(Long userId, LocalDateTime start, LocalDateTime end);

    Optional<Transaction> findByUser_UserIdAndId(Long userId, Long transactionId);
}
