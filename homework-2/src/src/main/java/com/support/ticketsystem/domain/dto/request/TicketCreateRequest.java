package com.support.ticketsystem.domain.dto.request;

import com.support.ticketsystem.domain.enums.Category;
import com.support.ticketsystem.domain.enums.Priority;
import com.support.ticketsystem.domain.enums.Status;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO for creating a new ticket.
 */
public record TicketCreateRequest(
    @NotBlank(message = "customer_id is required")
    @Size(max = 100, message = "customer_id must be at most 100 characters")
    String customerId,

    @NotBlank(message = "customer_email is required")
    @Email(message = "customer_email must be a valid email address")
    @Size(max = 255, message = "customer_email must be at most 255 characters")
    String customerEmail,

    @NotBlank(message = "customer_name is required")
    @Size(max = 200, message = "customer_name must be at most 200 characters")
    String customerName,

    @NotBlank(message = "subject is required")
    @Size(min = 1, max = 200, message = "subject must be between 1 and 200 characters")
    String subject,

    @NotBlank(message = "description is required")
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
