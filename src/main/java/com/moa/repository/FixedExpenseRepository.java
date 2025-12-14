package com.moa.repository;

import com.moa.entity.FixedExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FixedExpenseRepository extends JpaRepository<FixedExpense,Long> {
}
