package com.example.expense_tracker.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.expense_tracker.dto.AiCategoryResult;
import com.example.expense_tracker.dto.ExpenseRequestDTO;
import com.example.expense_tracker.dto.ExpenseUpdateDTO;
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

        expense.setCategory(result.getCategory().name());
        expense.setCategoryConfidence(result.getConfidence());


        return expenseRepository.save(expense);
    }

    public List<Expense> listExpenses() {
        return expenseRepository.findAll();
    }

    public Expense getExpense(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Expense not found"));
    }

    public Expense updateExpense(Long id, ExpenseUpdateDTO dto) {
        Expense expense = getExpense(id);

        boolean shouldRecategorize = false;
        if (dto.getAmount() != null && !Objects.equals(dto.getAmount(), expense.getAmount())) {
            expense.setAmount(dto.getAmount());
            shouldRecategorize = true;
        }

        if (dto.getDescription() != null && !Objects.equals(dto.getDescription(), expense.getDescription())) {
            expense.setDescription(dto.getDescription());
            shouldRecategorize = true;
        }

        if (dto.getDate() != null) {
            expense.setDate(dto.getDate());
        }

        if (shouldRecategorize) {
            AiCategoryResult result = aiService.categorize(
                    expense.getDescription(),
                    expense.getAmount());
            expense.setCategory(result.getCategory().name());
            expense.setCategoryConfidence(result.getConfidence());
        }

        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        Expense expense = getExpense(id);
        expenseRepository.delete(expense);
    }
}
