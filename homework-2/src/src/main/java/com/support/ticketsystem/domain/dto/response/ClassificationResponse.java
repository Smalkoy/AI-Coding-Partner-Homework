package com.support.ticketsystem.domain.dto.response;

import com.support.ticketsystem.domain.enums.Category;
import com.support.ticketsystem.domain.enums.Priority;

import java.util.List;

/**
 * DTO for ticket classification results.
 */
public record ClassificationResponse(
    Category category,
    Priority priority,
    double confidence,
    String reasoning,
    List<String> keywordsFound
) {

    /**
     * Creates a classification response with computed reasoning.
     */
    public static ClassificationResponse of(
            Category category,
            Priority priority,
            double confidence,
            List<String> categoryKeywords,
            List<String> priorityKeywords) {

        List<String> allKeywords = new java.util.ArrayList<>();
        allKeywords.addAll(categoryKeywords);
        allKeywords.addAll(priorityKeywords);

        String reasoning = buildReasoning(category, priority, categoryKeywords, priorityKeywords, confidence);

        return new ClassificationResponse(
            category,
            priority,
            confidence,
            reasoning,
            allKeywords
        );
    }

    private static String buildReasoning(
            Category category,
            Priority priority,
            List<String> categoryKeywords,
            List<String> priorityKeywords,
            double confidence) {

        StringBuilder sb = new StringBuilder();

        // Category reasoning
        if (categoryKeywords.isEmpty()) {
            sb.append("Category set to '").append(category.getValue())
              .append("' (default, no matching keywords found). ");
        } else {
            sb.append("Category '").append(category.getValue())
              .append("' detected based on keywords: ")
              .append(String.join(", ", categoryKeywords)).append(". ");
        }

        // Priority reasoning
        if (priorityKeywords.isEmpty()) {
            sb.append("Priority set to '").append(priority.getValue())
              .append("' (default). ");
        } else {
            sb.append("Priority '").append(priority.getValue())
              .append("' detected based on keywords: ")
              .append(String.join(", ", priorityKeywords)).append(". ");
        }

        // Confidence reasoning
        if (confidence >= 0.8) {
            sb.append("High confidence classification.");
        } else if (confidence >= 0.5) {
            sb.append("Moderate confidence classification.");
        } else {
            sb.append("Low confidence - manual review recommended.");
        }

        return sb.toString();
    }
}
