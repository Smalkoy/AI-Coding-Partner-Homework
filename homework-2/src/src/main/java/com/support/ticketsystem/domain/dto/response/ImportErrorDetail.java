package com.support.ticketsystem.domain.dto.response;

/**
 * DTO for import error details.
 */
public record ImportErrorDetail(
    int recordIndex,
    String field,
    String message,
    String rejectedValue
) {

    /**
     * Creates an error detail for a specific field validation error.
     */
    public static ImportErrorDetail fieldError(int index, String field, String message, String value) {
        return new ImportErrorDetail(index, field, message, value);
    }

    /**
     * Creates an error detail for a general record error.
     */
    public static ImportErrorDetail recordError(int index, String message) {
        return new ImportErrorDetail(index, null, message, null);
    }
}
