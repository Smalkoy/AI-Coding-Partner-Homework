package com.support.ticketsystem.domain.dto.response;

import com.support.ticketsystem.domain.enums.Category;
import com.support.ticketsystem.domain.enums.Priority;
import com.support.ticketsystem.domain.enums.Status;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for ticket responses.
 */
public record TicketResponse(
    UUID id,
    String customerId,
    String customerEmail,
    String customerName,
    String subject,
    String description,
    Category category,
    Priority priority,
    Status status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    OffsetDateTime resolvedAt,
    String assignedTo,
    List<String> tags,
    MetadataResponse metadata
) {
}
