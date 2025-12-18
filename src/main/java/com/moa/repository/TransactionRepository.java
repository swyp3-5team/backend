package com.moa.repository;

import com.moa.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUser_UserIdAndTransactionDateBetween(Long user_userId, LocalDate transactionDate, LocalDate transactionDate2);

    Optional<Transaction> findByUser_UserIdAndId(Long userId, Long transactionId);
}
