package com.support.ticketsystem.domain.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO for error responses.
 */
public record ErrorResponse(
    String error,
    String message,
    OffsetDateTime timestamp,
    String path,
    List<ValidationErrorDetail> details
) {
}
