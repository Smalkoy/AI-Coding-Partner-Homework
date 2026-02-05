package com.support.ticketsystem.controller;

import com.support.ticketsystem.domain.dto.request.TicketCreateRequest;
import com.support.ticketsystem.domain.dto.request.TicketUpdateRequest;
import com.support.ticketsystem.domain.dto.response.ClassificationResponse;
import com.support.ticketsystem.domain.dto.response.TicketResponse;
import com.support.ticketsystem.domain.entity.Ticket;
import com.support.ticketsystem.domain.enums.Category;
import com.support.ticketsystem.domain.enums.Priority;
import com.support.ticketsystem.domain.enums.Status;
import com.support.ticketsystem.service.ClassificationService;
import com.support.ticketsystem.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for ticket operations.
 */
@RestController
@RequestMapping("/tickets")
@Tag(name = "Tickets", description = "Ticket management endpoints")
public class TicketController {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);

    private final TicketService ticketService;
    private final ClassificationService classificationService;

    public TicketController(TicketService ticketService, ClassificationService classificationService) {
        this.ticketService = ticketService;
        this.classificationService = classificationService;
    }

    /**
     * Creates a new ticket.
     *
     * @param request      the ticket creation request
     * @param autoClassify whether to auto-classify the ticket
     * @return the created ticket
     */
    @PostMapping
    @Operation(summary = "Create a new ticket", description = "Creates a new support ticket")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Ticket created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody TicketCreateRequest request,
            @Parameter(description = "Auto-classify the ticket after creation")
            @RequestParam(value = "autoClassify", defaultValue = "false") boolean autoClassify) {

        log.info("POST /tickets - Creating ticket for customer: {}, autoClassify: {}",
            request.customerId(), autoClassify);

        TicketResponse response = ticketService.createTicket(request, autoClassify);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets a ticket by ID.
     *
     * @param id the ticket ID
     * @return the ticket
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID", description = "Retrieves a specific ticket by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket found"),
        @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketResponse> getTicket(
            @Parameter(description = "Ticket ID") @PathVariable UUID id) {

        log.info("GET /tickets/{} - Fetching ticket", id);

        TicketResponse response = ticketService.getTicket(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all tickets with optional filtering.
     *
     * @param category   optional category filter
     * @param priority   optional priority filter
     * @param status     optional status filter
     * @param customerId optional customer ID filter
     * @return list of tickets
     */
    @GetMapping
    @Operation(summary = "List all tickets", description = "Lists all tickets with optional filtering")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of tickets")
    })
    public ResponseEntity<List<TicketResponse>> getAllTickets(
            @Parameter(description = "Filter by category")
            @RequestParam(required = false) Category category,

            @Parameter(description = "Filter by priority")
            @RequestParam(required = false) Priority priority,

            @Parameter(description = "Filter by status")
            @RequestParam(required = false) Status status,

            @Parameter(description = "Filter by customer ID")
            @RequestParam(required = false) String customerId) {

        log.info("GET /tickets - Listing tickets with filters: category={}, priority={}, status={}, customerId={}",
            category, priority, status, customerId);

        List<TicketResponse> tickets = ticketService.getAllTickets(status, category, priority, customerId);
        return ResponseEntity.ok(tickets);
    }

    /**
     * Updates an existing ticket.
     *
     * @param id      the ticket ID
     * @param request the update request
     * @return the updated ticket
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update ticket", description = "Updates an existing ticket")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ticket updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketResponse> updateTicket(
            @Parameter(description = "Ticket ID") @PathVariable UUID id,
            @Valid @RequestBody TicketUpdateRequest request) {

        log.info("PUT /tickets/{} - Updating ticket", id);

        TicketResponse response = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a ticket.
     *
     * @param id the ticket ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ticket", description = "Deletes a ticket")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Ticket deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<Void> deleteTicket(
            @Parameter(description = "Ticket ID") @PathVariable UUID id) {

        log.info("DELETE /tickets/{} - Deleting ticket", id);

        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Auto-classifies a ticket based on its content.
     *
     * @param id the ticket ID
     * @return the classification result
     */
    @PostMapping("/{id}/auto-classify")
    @Operation(
        summary = "Auto-classify ticket",
        description = "Automatically categorizes and prioritizes a ticket based on its content. " +
                      "Updates the ticket with the classification results."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Classification successful"),
        @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @Tag(name = "Classification", description = "Ticket classification endpoints")
    public ResponseEntity<ClassificationResponse> autoClassifyTicket(
            @Parameter(description = "Ticket ID") @PathVariable UUID id) {

        log.info("POST /tickets/{}/auto-classify - Auto-classifying ticket", id);

        // Get the ticket
        Ticket ticket = ticketService.getTicketEntity(id);

        // Classify the ticket
        ClassificationResponse classification = classificationService.classify(ticket);

        // Apply classification to ticket
        classificationService.applyClassification(ticket, classification);

        // Save the updated ticket
        ticketService.saveTicket(ticket);

        log.info("Ticket {} classified: category={}, priority={}, confidence={}",
            id, classification.category(), classification.priority(), classification.confidence());

        return ResponseEntity.ok(classification);
    }
}
