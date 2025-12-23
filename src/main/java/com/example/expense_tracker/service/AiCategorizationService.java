package com.example.expense_tracker.service;

import org.springframework.stereotype.Service;

import com.example.expense_tracker.dto.AiCategoryResult;

@Service
public class AiCategorizationService {

    public AiCategoryResult categorize(String description, Double amount) {

        if (description == null) {
            return new AiCategoryResult("Misc", 0.5);
        }

        String d = description.toLowerCase();

        if (d.contains("zomato") || d.contains("swiggy") || d.contains("food")) {
            return new AiCategoryResult("Food", 0.9);
        }

        if (d.contains("uber") || d.contains("ola") || d.contains("cab")) {
            return new AiCategoryResult("Travel", 0.9);
        }

        if (amount != null && amount > 20000) {
            return new AiCategoryResult("Rent", 0.7);
        }

        return new AiCategoryResult("Misc", 0.6);
    }
}
