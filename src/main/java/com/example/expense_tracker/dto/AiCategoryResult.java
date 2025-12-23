package com.example.expense_tracker.dto;

import com.example.expense_tracker.domain.ExpenseCategory;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiCategoryResult {
    private ExpenseCategory category;
    private Double confidence;
}
