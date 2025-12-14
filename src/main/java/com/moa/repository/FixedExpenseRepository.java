package com.moa.repository;

import com.moa.entity.FixedExpense;
import com.moa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FixedExpenseRepository extends JpaRepository<FixedExpense,Long> {
    List<FixedExpense> user(User user);

    List<FixedExpense> findByUserIdAndIsActiveTrue(Long userId);
}
