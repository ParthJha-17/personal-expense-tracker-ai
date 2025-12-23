package com.example.expense_tracker.service;

import java.io.IOException;
import java.util.EnumSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.expense_tracker.domain.ExpenseCategory;
import com.example.expense_tracker.dto.AiCategoryResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.*;

@Service
public class AiCategorizationService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    public AiCategoryResult categorize(String description, Double amount) {
        try {
            String prompt = buildPrompt(description, amount);
            String response = callOpenAI(prompt);
            return parseAndValidate(response);
        } catch (Exception e) {
            return new AiCategoryResult(ExpenseCategory.MISC, 0.4);
        }
    }

    private String buildPrompt(String description, Double amount) {
        return """
        You are an expense categorization engine.

        Allowed categories:
        FOOD, TRAVEL, RENT, GROCERIES, HEALTH, INVESTMENT, SAVINGS, MISC

        Return ONLY valid JSON with fields:
        - category (one of the allowed categories)
        - confidence (number between 0 and 1)

        No extra text. No markdown.

        Expense:
        description="%s"
        amount=%s
        """.formatted(description, amount);
    }

    private String callOpenAI(String prompt) throws IOException {
        String body = """
        {
          "model": "%s",
          "messages": [
            { "role": "user", "content": "%s" }
          ],
          "temperature": 0.2
        }
        """.formatted(model, prompt.replace("\"", "\\\""));

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .post(RequestBody.create(body, JSON))
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OpenAI error: " + response);
            }
            return response.body().string();
        }
    }

    private AiCategoryResult parseAndValidate(String responseJson) throws IOException {
        JsonNode root = mapper.readTree(responseJson);
        String content = root
                .get("choices").get(0)
                .get("message").get("content")
                .asText();

        JsonNode result = mapper.readTree(content);

        String categoryStr = result.get("category").asText();
        double confidence = result.get("confidence").asDouble();

        ExpenseCategory category = EnumSet
                .allOf(ExpenseCategory.class)
                .stream()
                .filter(c -> c.name().equalsIgnoreCase(categoryStr))
                .findFirst()
                .orElse(ExpenseCategory.MISC);

        return new AiCategoryResult(category, confidence);
    }
}
