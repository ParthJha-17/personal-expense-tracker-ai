package com.example.expense_tracker.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.expense_tracker.dto.AiCategoryResult;
import com.example.expense_tracker.dto.ExpenseRequestDTO;
import com.example.expense_tracker.entity.Expense;
import com.example.expense_tracker.repository.ExpenseRepository;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    private final AiCategorizationService aiService;

    public ExpenseService(ExpenseRepository expenseRepository, AiCategorizationService aiService) {
        this.expenseRepository = expenseRepository;
        this.aiService = aiService;
    }

    public Expense addExpense(ExpenseRequestDTO dto) {
        Expense expense = new Expense();

        expense.setAmount(dto.getAmount());
        expense.setDescription(dto.getDescription());
        expense.setDate(dto.getDate());
        expense.setCreatedAt(LocalDateTime.now());

        // temporary (until auth)
        expense.setUserId(1L);

        AiCategoryResult result =
        aiService.categorize(dto.getDescription(), dto.getAmount());

        expense.setCategory(result.getCategory());
        expense.setCategoryConfidence(result.getConfidence());

        return expenseRepository.save(expense);
    }
}
