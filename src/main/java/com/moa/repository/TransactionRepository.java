package com.moa.repository;

import com.moa.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Object> findByIdAndTransactionGroupId(Long id,Long transactionGroupId);

    List<Transaction> findByTransactionGroupId(Long transactionGroupId);
}
