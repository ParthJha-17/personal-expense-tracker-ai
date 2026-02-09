package com.example.expense_tracker.dto;

import java.util.List;

import lombok.Data;

@Data
public class RecommendationResponseDTO {

    private String model;
    private String rationale;
    private List<String> recommendations;
}
