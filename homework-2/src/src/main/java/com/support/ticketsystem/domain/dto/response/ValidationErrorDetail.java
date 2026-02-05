package com.support.ticketsystem.domain.dto.response;

/**
 * DTO for validation error details.
 */
public record ValidationErrorDetail(
    String field,
    String message,
    Object rejectedValue
) {
}
