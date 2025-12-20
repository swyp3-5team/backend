package com.moa.repository;

import com.moa.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 거래내역 Repository
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
