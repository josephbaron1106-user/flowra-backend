package com.flowra.flowra_backend.repository;

import com.flowra.flowra_backend.entity.DailyExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailyExpenseRepository extends JpaRepository<DailyExpense, Long> {
    List<DailyExpense> findByCategory(String category);
}
