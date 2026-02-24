package com.support.ticketsystem.domain.dto.request;

import com.support.ticketsystem.domain.enums.Category;
import com.support.ticketsystem.domain.enums.Priority;
import com.support.ticketsystem.domain.enums.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for updating an existing ticket.
 * All fields are optional for partial updates.
 */
public record TicketUpdateRequest(
    @Size(min = 1, max = 200, message = "subject must be between 1 and 200 characters")
    String subject,

    @Size(min = 10, max = 2000, message = "description must be between 10 and 2000 characters")
    String description,

    Category category,

    Priority priority,

    Status status,

    @Size(max = 100, message = "assigned_to must be at most 100 characters")
    String assignedTo,

    List<@Size(max = 50, message = "each tag must be at most 50 characters") String> tags,

    @Valid
    MetadataDto metadata
) {
}
