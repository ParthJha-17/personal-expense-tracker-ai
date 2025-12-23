package com.example.expense_tracker.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expense_tracker.dto.ExpenseRequestDTO;
import com.example.expense_tracker.entity.Expense;
import com.example.expense_tracker.service.ExpenseService;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // here we should return DTO, will do in code refactoring
    @PostMapping
    public Expense addExpense(@RequestBody ExpenseRequestDTO dto) {
        return expenseService.addExpense(dto);
    }
}
