package com.example.expense_tracker.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ExpenseUpdateDTO {

    private Double amount;
    private String description;
    private LocalDate date;
}
