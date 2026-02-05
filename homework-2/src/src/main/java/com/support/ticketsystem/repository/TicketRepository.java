package com.support.ticketsystem.repository;

import com.support.ticketsystem.domain.entity.Ticket;
import com.support.ticketsystem.domain.enums.Category;
import com.support.ticketsystem.domain.enums.Priority;
import com.support.ticketsystem.domain.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Ticket entity with custom query methods.
 */
@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    /**
     * Find tickets by status.
     */
    List<Ticket> findByStatus(Status status);

    /**
     * Find tickets by category.
     */
    List<Ticket> findByCategory(Category category);

    /**
     * Find tickets by priority.
     */
    List<Ticket> findByPriority(Priority priority);

    /**
     * Find tickets by customer ID.
     */
    List<Ticket> findByCustomerId(String customerId);

    /**
     * Find tickets by status and category.
     */
    List<Ticket> findByStatusAndCategory(Status status, Category category);

    /**
     * Find tickets by status and priority.
     */
    List<Ticket> findByStatusAndPriority(Status status, Priority priority);

    /**
     * Find tickets by category and priority.
     */
    List<Ticket> findByCategoryAndPriority(Category category, Priority priority);

    /**
     * Custom query for combined filtering with optional parameters.
     * All parameters are optional - null values are ignored.
     */
    @Query("SELECT t FROM Ticket t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:customerId IS NULL OR t.customerId = :customerId) " +
           "ORDER BY t.createdAt DESC")
    List<Ticket> findByFilters(
        @Param("status") Status status,
        @Param("category") Category category,
        @Param("priority") Priority priority,
        @Param("customerId") String customerId
    );

    /**
     * Count tickets by status.
     */
    long countByStatus(Status status);

    /**
     * Count tickets by category.
     */
    long countByCategory(Category category);

    /**
     * Count tickets by priority.
     */
    long countByPriority(Priority priority);
}
