package com.moa.repository;

import com.moa.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget,Integer> {
    @Query("SELECT b FROM Budget b WHERE b.user.userId = :userId AND b.startDate <= :date AND (b.endDate IS NULL OR b.endDate > :date)")
    List<Budget> findBudgetsByDate(LocalDate date, Long userId);

    Optional<Budget> findByBudgetIdAndUserId(Long aLong, Long userId);
}
