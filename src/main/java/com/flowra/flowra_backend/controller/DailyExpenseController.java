package com.flowra.flowra_backend.controller;

import com.flowra.flowra_backend.entity.DailyExpense;
import com.flowra.flowra_backend.repository.DailyExpenseRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class DailyExpenseController {

    private final DailyExpenseRepository expenseRepository;

    @Data
    public static class ExpenseRequest {
        private String expenseCode;
        private String category;
        private String icon;
        private String description;
        private BigDecimal amount;
        private String paymentMode;
        private String spentBy;
        private String expenseDate;
    }

    @PostMapping
    public ResponseEntity<DailyExpense> addExpense(@RequestBody ExpenseRequest req) {
        LocalDate date = LocalDate.now();
        if (req.getExpenseDate() != null && !req.getExpenseDate().isBlank()) {
            try { date = LocalDate.parse(req.getExpenseDate()); } catch (Exception ignored) {}
        }

        String code = req.getExpenseCode();
        if (code == null || code.isBlank()) {
            code = "EXP-" + System.currentTimeMillis() % 100000;
        }

        DailyExpense expense = DailyExpense.builder()
                .expenseCode(code)
                .category(req.getCategory() != null ? req.getCategory() : "General")
                .icon(req.getIcon() != null ? req.getIcon() : "💸")
                .description(req.getDescription())
                .amount(req.getAmount() != null ? req.getAmount() : BigDecimal.ZERO)
                .paymentMode(req.getPaymentMode() != null ? req.getPaymentMode() : "Cash from Drawer")
                .spentBy(req.getSpentBy() != null ? req.getSpentBy() : "Staff")
                .expenseDate(date)
                .build();

        return new ResponseEntity<>(expenseRepository.save(expense), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DailyExpense>> getAllExpenses() {
        return ResponseEntity.ok(expenseRepository.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id) {
        if (!expenseRepository.existsById(id)) return ResponseEntity.notFound().build();
        expenseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
