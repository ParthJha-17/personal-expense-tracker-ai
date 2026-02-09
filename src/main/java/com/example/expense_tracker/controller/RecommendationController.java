package com.example.expense_tracker.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.expense_tracker.dto.RecommendationRequestDTO;
import com.example.expense_tracker.dto.RecommendationResponseDTO;
import com.example.expense_tracker.service.RecommendationService;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public RecommendationResponseDTO recommend(@RequestBody RecommendationRequestDTO request) {
        return recommendationService.recommend(request);
    }
}
