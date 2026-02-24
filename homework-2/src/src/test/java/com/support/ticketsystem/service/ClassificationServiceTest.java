package com.support.ticketsystem.service;

import com.support.ticketsystem.config.ClassificationConfig;
import com.support.ticketsystem.domain.dto.response.ClassificationResponse;
import com.support.ticketsystem.domain.entity.Ticket;
import com.support.ticketsystem.domain.enums.Category;
import com.support.ticketsystem.domain.enums.Priority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ClassificationService Tests")
class ClassificationServiceTest {

    private ClassificationService classificationService;

    @BeforeEach
    void setUp() {
        ClassificationConfig config = new ClassificationConfig();
        Map<Category, List<String>> categoryKeywords = config.categoryKeywords();
        Map<Priority, List<String>> priorityKeywords = config.priorityKeywords();
        List<Priority> priorityOrder = config.priorityOrder();
        List<Category> categoryOrder = config.categoryOrder();

        classificationService = new ClassificationService(
            categoryKeywords, priorityKeywords, priorityOrder, categoryOrder
        );
    }

    @Test
    @DisplayName("Classify as account_access for login keywords")
    void testClassify_AccountAccess() {
        Ticket ticket = createTicket(
            "Cannot login to my account",
            "I am unable to login after changing my password. Access denied error appears."
        );

        ClassificationResponse result = classificationService.classify(ticket);

        assertThat(result.category()).isEqualTo(Category.ACCOUNT_ACCESS);
        assertThat(result.keywordsFound()).contains("login");
    }

    @Test
    @DisplayName("Classify as technical_issue for error keywords")
    void testClassify_TechnicalIssue() {
        Ticket ticket = createTicket(
            "Application crash on startup",
            "The application crashes every time I try to open it. Error message appears."
        );

        ClassificationResponse result = classificationService.classify(ticket);

        assertThat(result.category()).isEqualTo(Category.TECHNICAL_ISSUE);
        assertThat(result.keywordsFound()).anyMatch(k ->
            k.contains("crash") || k.contains("error")
        );
    }

    @Test
    @DisplayName("Classify as billing_question for invoice keywords")
    void testClassify_BillingQuestion() {
        Ticket ticket = createTicket(
            "Question about my invoice",
            "I have a question about the charges on my latest invoice. There seems to be a duplicate payment."
        );

        ClassificationResponse result = classificationService.classify(ticket);

        assertThat(result.category()).isEqualTo(Category.BILLING_QUESTION);
        assertThat(result.keywordsFound()).anyMatch(k ->
            k.contains("invoice") || k.contains("payment")
        );
    }

    @Test
    @DisplayName("Classify as feature_request for suggestion keywords")
    void testClassify_FeatureRequest() {
        Ticket ticket = createTicket(
            "Feature suggestion",
            "Would be nice to have a dark mode option. This enhancement would improve user experience."
        );

        ClassificationResponse result = classificationService.classify(ticket);

        assertThat(result.category()).isEqualTo(Category.FEATURE_REQUEST);
        assertThat(result.keywordsFound()).anyMatch(k ->
            k.contains("would be nice") || k.contains("enhancement") || k.contains("suggestion")
        );
    }

    @Test
    @DisplayName("Classify as bug_report for reproduction steps")
    void testClassify_BugReport() {
        Ticket ticket = createTicket(
            "Bug in search feature",
            "Found a defect. Steps to reproduce: 1. Search 2. Error. Expected: results. Actual: error."
        );

        ClassificationResponse result = classificationService.classify(ticket);

        assertThat(result.category()).isEqualTo(Category.BUG_REPORT);
        assertThat(result.keywordsFound()).anyMatch(k ->
            k.contains("reproduce") || k.contains("defect") || k.contains("expected")
        );
    }

    @Test
    @DisplayName("Classify as other when no keywords match")
    void testClassify_Other_NoMatch() {
        Ticket ticket = createTicket(
            "General inquiry",
            "Hello, I would like to know more about your services and how they work."
        );

        ClassificationResponse result = classificationService.classify(ticket);

        assertThat(result.category()).isEqualTo(Category.OTHER);
        assertThat(result.confidence()).isLessThan(0.5);
    }

    @Test
    @DisplayName("Classify as urgent priority")
    void testClassify_UrgentPriority() {
        Ticket ticket = createTicket(
            "CRITICAL: Production is down",
            "Our production system is completely down. This is a critical emergency!"
        );

        ClassificationResponse result = classificationService.classify(ticket);

        assertThat(result.priority()).isEqualTo(Priority.URGENT);
        assertThat(result.keywordsFound()).anyMatch(k ->
            k.contains("critical") || k.contains("production down") || k.contains("emergency")
        );
    }

    @Test
    @DisplayName("Classify as high priority")
    void testClassify_HighPriority() {
        Ticket ticket = createTicket(
            "Important: This is blocking my work",
            "This issue is blocking our team from working. We need it resolved soon."
        );

        ClassificationResponse result = classificationService.classify(ticket);

        assertThat(result.priority()).isEqualTo(Priority.HIGH);
        assertThat(result.keywordsFound()).anyMatch(k ->
            k.contains("important") || k.contains("blocking")
        );
    }

    @Test
    @DisplayName("Classify as low priority")
    void testClassify_LowPriority() {
        Ticket ticket = createTicket(
            "Minor cosmetic issue",
            "There is a minor typo on the settings page. No rush to fix this."
        );

        ClassificationResponse result = classificationService.classify(ticket);

        assertThat(result.priority()).isEqualTo(Priority.LOW);
        assertThat(result.keywordsFound()).anyMatch(k ->
            k.contains("minor") || k.contains("cosmetic") || k.contains("no rush")
        );
    }

    @Test
    @DisplayName("Classify as medium priority when no priority keywords")
    void testClassify_MediumPriority_Default() {
        Ticket ticket = createTicket(
            "Regular support question",
            "I have a question about how to use the export feature in the application."
        );

        ClassificationResponse result = classificationService.classify(ticket);

        assertThat(result.priority()).isEqualTo(Priority.MEDIUM);
    }

    @Test
    @DisplayName("Confidence score increases with multiple keyword matches")
    void testClassify_ConfidenceWithMultipleMatches() {
        Ticket ticket1 = createTicket(
            "Error",
            "There is an error in the system."
        );

        Ticket ticket2 = createTicket(
            "Critical error crash",
            "The application is crashing with errors. It is completely broken and not working."
        );

        ClassificationResponse result1 = classificationService.classify(ticket1);
        ClassificationResponse result2 = classificationService.classify(ticket2);

        assertThat(result2.confidence()).isGreaterThan(result1.confidence());
    }

    @Test
    @DisplayName("Classification includes reasoning")
    void testClassify_IncludesReasoning() {
        Ticket ticket = createTicket(
            "Cannot login",
            "I cannot access my account due to password issues."
        );

        ClassificationResponse result = classificationService.classify(ticket);

        assertThat(result.reasoning()).isNotEmpty();
        assertThat(result.reasoning()).contains("Category");
    }

    private Ticket createTicket(String subject, String description) {
        Ticket ticket = new Ticket();
        ticket.setSubject(subject);
        ticket.setDescription(description);
        return ticket;
    }
}
