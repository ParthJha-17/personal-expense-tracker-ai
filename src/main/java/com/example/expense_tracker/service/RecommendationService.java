package com.example.expense_tracker.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import com.example.expense_tracker.dto.RecommendationRequestDTO;
import com.example.expense_tracker.dto.RecommendationResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class RecommendationService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final RecommendationRagService recommendationRagService;

    private String promptTemplate;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.recommendations.prompt.template}")
    private Resource promptTemplateResource;

    public RecommendationService(RecommendationRagService recommendationRagService) {
        this.recommendationRagService = recommendationRagService;
    }

    @PostConstruct
    void loadPromptTemplate() throws IOException {
        this.promptTemplate = StreamUtils.copyToString(
                promptTemplateResource.getInputStream(),
                StandardCharsets.UTF_8);
    }

    public RecommendationResponseDTO recommend(RecommendationRequestDTO request) {
        try {
            String prompt = buildPrompt(request);
            String response = callOpenAI(prompt);
            return parseResponse(response);
        } catch (Exception e) {
            RecommendationResponseDTO fallback = new RecommendationResponseDTO();
            fallback.setModel("N/A");
            fallback.setRationale("Unable to generate recommendations.");
            fallback.setRecommendations(List.of());
            return fallback;
        }
    }

    private String buildPrompt(RecommendationRequestDTO request) {
        String query = String.join(" ", safeValue(request.getGoals()), safeValue(request.getProfile())).trim();
        String context = recommendationRagService.buildContext(query);
        if (context.isBlank()) {
            context = "No additional context.";
        }

        return promptTemplate
                .replace("{{goals}}", defaultText(request.getGoals()))
                .replace("{{profile}}", defaultText(request.getProfile()))
                .replace("{{context}}", context);
    }

    private String defaultText(String value) {
        return value == null || value.isBlank() ? "Not provided." : value;
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private String callOpenAI(String prompt) throws IOException {
        ObjectNode payload = mapper.createObjectNode();
        payload.put("model", model);
        ArrayNode messages = payload.putArray("messages");
        ObjectNode message = mapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        payload.put("temperature", 0.3);

        String body = mapper.writeValueAsString(payload);

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

    private RecommendationResponseDTO parseResponse(String responseJson) throws IOException {
        JsonNode root = mapper.readTree(responseJson);
        String content = root
                .get("choices").get(0)
                .get("message").get("content")
                .asText();

        JsonNode result = mapper.readTree(content);
        RecommendationResponseDTO response = new RecommendationResponseDTO();
        response.setModel(result.path("model").asText());
        response.setRationale(result.path("rationale").asText());

        List<String> recommendations = mapper.convertValue(
                result.path("recommendations"),
                mapper.getTypeFactory().constructCollectionType(List.class, String.class));
        response.setRecommendations(recommendations);
        return response;
    }
}
