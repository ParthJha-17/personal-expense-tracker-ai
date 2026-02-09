package com.example.expense_tracker.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
public class RecommendationRagService {

    @Value("${openai.recommendations.rag.enabled:false}")
    private boolean ragEnabled;

    @Value("${openai.recommendations.rag.knowledge-base:classpath:rag/personal-finance-models.md}")
    private Resource knowledgeBaseResource;

    @Value("${openai.recommendations.rag.max-snippets:4}")
    private int maxSnippets;

    private List<String> chunks = List.of();

    @PostConstruct
    void loadKnowledgeBase() throws IOException {
        if (!ragEnabled) {
            return;
        }

        String raw = StreamUtils.copyToString(
                knowledgeBaseResource.getInputStream(),
                StandardCharsets.UTF_8);

        List<String> parsedChunks = new ArrayList<>();
        String[] sections = raw.split("(?m)^## ");
        for (int i = 0; i < sections.length; i++) {
            String section = sections[i].trim();
            if (section.isBlank()) {
                continue;
            }
            String chunk = i == 0 ? section : "## " + section;
            parsedChunks.add(chunk.trim());
        }
        this.chunks = parsedChunks;
    }

    public String buildContext(String query) {
        if (!ragEnabled || chunks.isEmpty() || query == null || query.isBlank()) {
            return "";
        }

        Set<String> tokens = tokenize(query);
        return chunks.stream()
                .map(chunk -> new ScoredChunk(chunk, scoreChunk(chunk, tokens)))
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator.comparingInt(ScoredChunk::score).reversed())
                .limit(maxSnippets)
                .map(ScoredChunk::chunk)
                .collect(Collectors.joining("\n\n"));
    }

    private int scoreChunk(String chunk, Set<String> tokens) {
        String normalized = chunk.toLowerCase(Locale.ROOT);
        int score = 0;
        for (String token : tokens) {
            if (normalized.contains(token)) {
                score++;
            }
        }
        return score;
    }

    private Set<String> tokenize(String query) {
        return List.of(query.toLowerCase(Locale.ROOT).split("\\W+"))
                .stream()
                .filter(token -> token.length() > 2)
                .collect(Collectors.toSet());
    }

    private record ScoredChunk(String chunk, int score) {
    }
}
