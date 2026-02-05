package com.support.ticketsystem.service;

import com.support.ticketsystem.domain.dto.response.ClassificationResponse;
import com.support.ticketsystem.domain.entity.Ticket;
import com.support.ticketsystem.domain.enums.Category;
import com.support.ticketsystem.domain.enums.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for automatic ticket classification based on keyword matching.
 */
@Service
public class ClassificationService {

    private static final Logger log = LoggerFactory.getLogger(ClassificationService.class);

    // Maximum text length to process (for performance)
    private static final int MAX_TEXT_LENGTH = 5000;

    // Weight factors for confidence calculation
    private static final double SUBJECT_WEIGHT = 1.5;  // Keywords in subject are more significant
    private static final double DESCRIPTION_WEIGHT = 1.0;
    private static final double MULTIPLE_MATCH_BONUS = 0.1;  // Bonus for each additional keyword match

    private final Map<Category, List<String>> categoryKeywords;
    private final Map<Priority, List<String>> priorityKeywords;
    private final List<Priority> priorityOrder;
    private final List<Category> categoryOrder;

    // Precompiled patterns for each keyword (case-insensitive, word boundary)
    private final Map<Category, List<Pattern>> categoryPatterns;
    private final Map<Priority, List<Pattern>> priorityPatterns;

    public ClassificationService(
            Map<Category, List<String>> categoryKeywords,
            Map<Priority, List<String>> priorityKeywords,
            List<Priority> priorityOrder,
            List<Category> categoryOrder) {
        this.categoryKeywords = categoryKeywords;
        this.priorityKeywords = priorityKeywords;
        this.priorityOrder = priorityOrder;
        this.categoryOrder = categoryOrder;

        // Precompile patterns for performance
        this.categoryPatterns = compilePatterns(categoryKeywords);
        this.priorityPatterns = compilePatterns(priorityKeywords);

        log.info("ClassificationService initialized with {} category mappings and {} priority mappings",
            categoryKeywords.size(), priorityKeywords.size());
    }

    /**
     * Classifies a ticket based on its subject and description.
     *
     * @param ticket the ticket to classify
     * @return classification result with category, priority, confidence, and reasoning
     */
    public ClassificationResponse classify(Ticket ticket) {
        log.info("Classifying ticket: {}", ticket.getId());

        String subject = normalizeText(ticket.getSubject());
        String description = normalizeText(ticket.getDescription());

        // Find category
        CategoryMatch categoryMatch = findCategory(subject, description);
        log.debug("Category match: {} with {} keywords", categoryMatch.category, categoryMatch.keywords.size());

        // Find priority
        PriorityMatch priorityMatch = findPriority(subject, description);
        log.debug("Priority match: {} with {} keywords", priorityMatch.priority, priorityMatch.keywords.size());

        // Calculate confidence
        double confidence = calculateConfidence(categoryMatch, priorityMatch);
        log.debug("Confidence score: {}", confidence);

        // Build response
        ClassificationResponse response = ClassificationResponse.of(
            categoryMatch.category,
            priorityMatch.priority,
            confidence,
            categoryMatch.keywords,
            priorityMatch.keywords
        );

        log.info("Classification complete for ticket {}: category={}, priority={}, confidence={}",
            ticket.getId(), categoryMatch.category, priorityMatch.priority, confidence);

        return response;
    }

    /**
     * Applies classification results to a ticket entity.
     *
     * @param ticket   the ticket to update
     * @param response the classification response
     */
    public void applyClassification(Ticket ticket, ClassificationResponse response) {
        ticket.setCategory(response.category());
        ticket.setPriority(response.priority());
        log.debug("Applied classification to ticket {}: category={}, priority={}",
            ticket.getId(), response.category(), response.priority());
    }

    private CategoryMatch findCategory(String subject, String description) {
        Map<Category, List<String>> matches = new EnumMap<>(Category.class);

        // Check each category in order
        for (Category category : categoryOrder) {
            List<Pattern> patterns = categoryPatterns.get(category);
            if (patterns == null || patterns.isEmpty()) {
                continue;
            }

            List<String> matchedKeywords = new ArrayList<>();

            // Check subject (higher weight)
            for (int i = 0; i < patterns.size(); i++) {
                Pattern pattern = patterns.get(i);
                if (pattern.matcher(subject).find()) {
                    matchedKeywords.add(categoryKeywords.get(category).get(i));
                }
            }

            // Check description
            for (int i = 0; i < patterns.size(); i++) {
                Pattern pattern = patterns.get(i);
                String keyword = categoryKeywords.get(category).get(i);
                if (!matchedKeywords.contains(keyword) && pattern.matcher(description).find()) {
                    matchedKeywords.add(keyword);
                }
            }

            if (!matchedKeywords.isEmpty()) {
                matches.put(category, matchedKeywords);
            }
        }

        // Select best category (most specific with matches)
        for (Category category : categoryOrder) {
            if (matches.containsKey(category)) {
                return new CategoryMatch(category, matches.get(category));
            }
        }

        // Default to OTHER
        return new CategoryMatch(Category.OTHER, List.of());
    }

    private PriorityMatch findPriority(String subject, String description) {
        // Check priorities in order (URGENT -> HIGH -> LOW -> MEDIUM)
        for (Priority priority : priorityOrder) {
            List<Pattern> patterns = priorityPatterns.get(priority);
            if (patterns == null || patterns.isEmpty()) {
                continue;
            }

            List<String> matchedKeywords = new ArrayList<>();

            // Check subject first
            for (int i = 0; i < patterns.size(); i++) {
                Pattern pattern = patterns.get(i);
                if (pattern.matcher(subject).find()) {
                    matchedKeywords.add(priorityKeywords.get(priority).get(i));
                }
            }

            // Check description
            for (int i = 0; i < patterns.size(); i++) {
                Pattern pattern = patterns.get(i);
                String keyword = priorityKeywords.get(priority).get(i);
                if (!matchedKeywords.contains(keyword) && pattern.matcher(description).find()) {
                    matchedKeywords.add(keyword);
                }
            }

            if (!matchedKeywords.isEmpty()) {
                return new PriorityMatch(priority, matchedKeywords);
            }
        }

        // Default to MEDIUM
        return new PriorityMatch(Priority.MEDIUM, List.of());
    }

    private double calculateConfidence(CategoryMatch categoryMatch, PriorityMatch priorityMatch) {
        double confidence = 0.0;

        // Base confidence from category match
        if (!categoryMatch.keywords.isEmpty()) {
            confidence += 0.4;  // Base for having a category match
            confidence += Math.min(categoryMatch.keywords.size() * MULTIPLE_MATCH_BONUS, 0.2);
        } else {
            confidence += 0.1;  // Low confidence for default category
        }

        // Base confidence from priority match
        if (!priorityMatch.keywords.isEmpty()) {
            confidence += 0.3;  // Base for having a priority match
            confidence += Math.min(priorityMatch.keywords.size() * MULTIPLE_MATCH_BONUS, 0.1);
        } else {
            confidence += 0.1;  // Default priority is reasonable
        }

        // Bonus for having both matches
        if (!categoryMatch.keywords.isEmpty() && !priorityMatch.keywords.isEmpty()) {
            confidence += 0.1;
        }

        // Cap at 0.95 (never 100% certain)
        return Math.min(confidence, 0.95);
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }

        // Truncate if too long
        if (text.length() > MAX_TEXT_LENGTH) {
            text = text.substring(0, MAX_TEXT_LENGTH);
        }

        // Convert to lowercase for matching
        return text.toLowerCase();
    }

    private <T extends Enum<T>> Map<T, List<Pattern>> compilePatterns(Map<T, List<String>> keywordMap) {
        Map<T, List<Pattern>> patternMap = new HashMap<>();

        for (Map.Entry<T, List<String>> entry : keywordMap.entrySet()) {
            List<Pattern> patterns = new ArrayList<>();
            for (String keyword : entry.getValue()) {
                // Create pattern with word boundary matching
                // Use (?i) for case-insensitive and \b for word boundaries
                String regex = "(?i)\\b" + Pattern.quote(keyword) + "\\b";
                patterns.add(Pattern.compile(regex));
            }
            patternMap.put(entry.getKey(), patterns);
        }

        return patternMap;
    }

    /**
     * Internal record for category match results.
     */
    private record CategoryMatch(Category category, List<String> keywords) {}

    /**
     * Internal record for priority match results.
     */
    private record PriorityMatch(Priority priority, List<String> keywords) {}
}
