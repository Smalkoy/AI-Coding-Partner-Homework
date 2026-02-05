package com.support.ticketsystem.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing ticket status values.
 */
public enum Status {
    NEW("new"),
    IN_PROGRESS("in_progress"),
    WAITING_CUSTOMER("waiting_customer"),
    RESOLVED("resolved"),
    CLOSED("closed");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Status fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (Status status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
