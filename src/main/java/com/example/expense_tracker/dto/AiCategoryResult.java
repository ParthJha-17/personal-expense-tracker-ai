package com.example.expense_tracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiCategoryResult {
    private String category;
    private Double confidence;
}
