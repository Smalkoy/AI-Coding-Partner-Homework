package com.support.ticketsystem.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing device types.
 */
public enum DeviceType {
    DESKTOP("desktop"),
    MOBILE("mobile"),
    TABLET("tablet");

    private final String value;

    DeviceType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DeviceType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (DeviceType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown device type: " + value);
    }
}
