package com.support.ticketsystem.config;

import com.support.ticketsystem.domain.enums.Category;
import com.support.ticketsystem.domain.enums.Priority;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * Configuration for ticket classification keywords.
 */
@Configuration
public class ClassificationConfig {

    /**
     * Keyword mappings for category classification.
     * Keywords are matched case-insensitively against ticket subject and description.
     */
    @Bean
    public Map<Category, List<String>> categoryKeywords() {
        Map<Category, List<String>> keywords = new EnumMap<>(Category.class);

        keywords.put(Category.ACCOUNT_ACCESS, List.of(
            "login",
            "log in",
            "sign in",
            "signin",
            "password",
            "reset password",
            "forgot password",
            "2fa",
            "two-factor",
            "two factor",
            "authentication",
            "authenticator",
            "access denied",
            "locked out",
            "locked account",
            "can't access",
            "cannot access",
            "can't sign in",
            "cannot sign in",
            "can't log in",
            "cannot log in",
            "account locked",
            "unlock account",
            "mfa",
            "multi-factor",
            "verification code",
            "security code"
        ));

        keywords.put(Category.TECHNICAL_ISSUE, List.of(
            "error",
            "crash",
            "crashed",
            "crashing",
            "bug",
            "not working",
            "doesn't work",
            "does not work",
            "broken",
            "failed",
            "failure",
            "exception",
            "issue",
            "problem",
            "glitch",
            "malfunction",
            "slow",
            "loading",
            "timeout",
            "timed out",
            "freezing",
            "frozen",
            "unresponsive",
            "504",
            "500",
            "503",
            "internal server error"
        ));

        keywords.put(Category.BILLING_QUESTION, List.of(
            "invoice",
            "invoices",
            "payment",
            "payments",
            "charge",
            "charged",
            "charges",
            "refund",
            "refunds",
            "subscription",
            "billing",
            "billed",
            "price",
            "pricing",
            "cost",
            "receipt",
            "receipts",
            "credit card",
            "card declined",
            "transaction",
            "overcharged",
            "double charged",
            "cancel subscription",
            "upgrade",
            "downgrade",
            "plan",
            "trial"
        ));

        keywords.put(Category.FEATURE_REQUEST, List.of(
            "suggest",
            "suggestion",
            "feature",
            "enhancement",
            "would be nice",
            "would be great",
            "could you add",
            "can you add",
            "please add",
            "request",
            "requesting",
            "improvement",
            "improve",
            "wish",
            "wishlist",
            "idea",
            "proposal",
            "consider adding",
            "it would help",
            "would love",
            "be great if",
            "nice to have"
        ));

        keywords.put(Category.BUG_REPORT, List.of(
            "reproduce",
            "reproduction",
            "steps to reproduce",
            "repro steps",
            "expected",
            "actual",
            "expected behavior",
            "actual behavior",
            "defect",
            "regression",
            "worked before",
            "used to work",
            "no longer works",
            "stopped working",
            "broken since",
            "after update",
            "after upgrade",
            "version"
        ));

        // OTHER has no keywords - it's the default fallback
        keywords.put(Category.OTHER, List.of());

        return Collections.unmodifiableMap(keywords);
    }

    /**
     * Keyword mappings for priority classification.
     * Higher priority levels are checked first.
     */
    @Bean
    public Map<Priority, List<String>> priorityKeywords() {
        Map<Priority, List<String>> keywords = new EnumMap<>(Priority.class);

        keywords.put(Priority.URGENT, List.of(
            "urgent",
            "urgently",
            "critical",
            "critically",
            "emergency",
            "production down",
            "production is down",
            "site down",
            "site is down",
            "service down",
            "outage",
            "security",
            "security issue",
            "security vulnerability",
            "data breach",
            "data leak",
            "immediately",
            "asap",
            "as soon as possible",
            "can't access anything",
            "completely broken",
            "total failure",
            "all users affected",
            "business critical"
        ));

        keywords.put(Priority.HIGH, List.of(
            "important",
            "high priority",
            "blocking",
            "blocker",
            "blocked",
            "need soon",
            "needed soon",
            "need quickly",
            "time sensitive",
            "deadline",
            "affects many",
            "many users",
            "multiple users",
            "can't work",
            "cannot work",
            "preventing work",
            "major issue",
            "significant impact"
        ));

        keywords.put(Priority.LOW, List.of(
            "minor",
            "low priority",
            "cosmetic",
            "typo",
            "spelling",
            "suggestion",
            "when you get a chance",
            "whenever possible",
            "no rush",
            "not urgent",
            "nice to have",
            "small issue",
            "minor issue",
            "trivial",
            "inconvenience",
            "slightly"
        ));

        // MEDIUM has no keywords - it's the default
        keywords.put(Priority.MEDIUM, List.of());

        return Collections.unmodifiableMap(keywords);
    }

    /**
     * Priority order for classification (highest to lowest).
     */
    @Bean
    public List<Priority> priorityOrder() {
        return List.of(Priority.URGENT, Priority.HIGH, Priority.LOW, Priority.MEDIUM);
    }

    /**
     * Category order for classification (most specific to least specific).
     */
    @Bean
    public List<Category> categoryOrder() {
        return List.of(
            Category.BUG_REPORT,      // Most specific (has reproduction steps)
            Category.ACCOUNT_ACCESS,   // Specific domain
            Category.BILLING_QUESTION, // Specific domain
            Category.FEATURE_REQUEST,  // Has specific patterns
            Category.TECHNICAL_ISSUE,  // Broad technical
            Category.OTHER             // Fallback
        );
    }
}
