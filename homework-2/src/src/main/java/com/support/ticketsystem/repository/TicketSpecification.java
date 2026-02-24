package com.support.ticketsystem.repository;

import com.support.ticketsystem.domain.entity.Ticket;
import com.support.ticketsystem.domain.enums.Category;
import com.support.ticketsystem.domain.enums.Priority;
import com.support.ticketsystem.domain.enums.Status;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for dynamic Ticket queries.
 */
public final class TicketSpecification {

    private TicketSpecification() {
        // Utility class - prevent instantiation
    }

    /**
     * Specification for filtering by status.
     */
    public static Specification<Ticket> hasStatus(Status status) {
        return (root, query, cb) ->
            status == null ? null : cb.equal(root.get("status"), status);
    }

    /**
     * Specification for filtering by category.
     */
    public static Specification<Ticket> hasCategory(Category category) {
        return (root, query, cb) ->
            category == null ? null : cb.equal(root.get("category"), category);
    }

    /**
     * Specification for filtering by priority.
     */
    public static Specification<Ticket> hasPriority(Priority priority) {
        return (root, query, cb) ->
            priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    /**
     * Specification for filtering by customer ID.
     */
    public static Specification<Ticket> hasCustomerId(String customerId) {
        return (root, query, cb) ->
            customerId == null || customerId.isBlank() ? null : cb.equal(root.get("customerId"), customerId);
    }

    /**
     * Combines multiple specifications with AND logic.
     */
    public static Specification<Ticket> withFilters(
            Status status, Category category, Priority priority, String customerId) {
        return Specification.where(hasStatus(status))
            .and(hasCategory(category))
            .and(hasPriority(priority))
            .and(hasCustomerId(customerId));
    }
}
