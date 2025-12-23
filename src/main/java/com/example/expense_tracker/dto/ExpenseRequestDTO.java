package com.example.expense_tracker.dto;
import java.time.LocalDate;

import lombok.Data;
@Data
public class ExpenseRequestDTO {

    private Double amount;
    private String description;
    private LocalDate date;

    // getters & setters (or Lombok later)
}
