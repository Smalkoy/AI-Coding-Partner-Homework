package com.support.ticketsystem.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing ticket categories.
 */
public enum Category {
    ACCOUNT_ACCESS("account_access"),
    TECHNICAL_ISSUE("technical_issue"),
    BILLING_QUESTION("billing_question"),
    FEATURE_REQUEST("feature_request"),
    BUG_REPORT("bug_report"),
    OTHER("other");

    private final String value;

    Category(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Category fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (Category category : values()) {
            if (category.value.equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + value);
    }
}
