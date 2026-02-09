package com.example.expense_tracker.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expense_tracker.dto.ExpenseRequestDTO;
import com.example.expense_tracker.dto.ExpenseUpdateDTO;
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

    @GetMapping
    public List<Expense> listExpenses() {
        return expenseService.listExpenses();
    }

    @GetMapping("/{id}")
    public Expense getExpense(@PathVariable Long id) {
        return expenseService.getExpense(id);
    }

    @PutMapping("/{id}")
    public Expense updateExpense(@PathVariable Long id, @RequestBody ExpenseUpdateDTO dto) {
        return expenseService.updateExpense(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
    }
}
