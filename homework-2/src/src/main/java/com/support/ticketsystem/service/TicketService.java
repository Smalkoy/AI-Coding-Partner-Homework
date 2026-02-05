package com.support.ticketsystem.service;

import com.support.ticketsystem.domain.dto.request.TicketCreateRequest;
import com.support.ticketsystem.domain.dto.request.TicketUpdateRequest;
import com.support.ticketsystem.domain.dto.response.ClassificationResponse;
import com.support.ticketsystem.domain.dto.response.TicketResponse;
import com.support.ticketsystem.domain.entity.Ticket;
import com.support.ticketsystem.domain.enums.Category;
import com.support.ticketsystem.domain.enums.Priority;
import com.support.ticketsystem.domain.enums.Status;
import com.support.ticketsystem.exception.TicketNotFoundException;
import com.support.ticketsystem.mapper.TicketMapper;
import com.support.ticketsystem.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for ticket operations.
 */
@Service
@Transactional
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final ClassificationService classificationService;

    public TicketService(TicketRepository ticketRepository,
                         TicketMapper ticketMapper,
                         @Lazy ClassificationService classificationService) {
        this.ticketRepository = ticketRepository;
        this.ticketMapper = ticketMapper;
        this.classificationService = classificationService;
    }

    /**
     * Creates a new ticket.
     *
     * @param request the ticket creation request
     * @return the created ticket response
     */
    public TicketResponse createTicket(TicketCreateRequest request) {
        return createTicket(request, false);
    }

    /**
     * Creates a new ticket with optional auto-classification.
     *
     * @param request      the ticket creation request
     * @param autoClassify whether to auto-classify the ticket
     * @return the created ticket response
     */
    public TicketResponse createTicket(TicketCreateRequest request, boolean autoClassify) {
        log.info("Creating new ticket for customer: {}, autoClassify: {}", request.customerId(), autoClassify);

        Ticket ticket = ticketMapper.toEntity(request);

        // Auto-classify if requested and category/priority not explicitly provided
        if (autoClassify) {
            boolean categoryProvided = request.category() != null;
            boolean priorityProvided = request.priority() != null;

            if (!categoryProvided || !priorityProvided) {
                log.debug("Auto-classifying ticket during creation");
                ClassificationResponse classification = classificationService.classify(ticket);

                // Only apply classification for fields not explicitly provided
                if (!categoryProvided) {
                    ticket.setCategory(classification.category());
                    log.debug("Auto-classified category: {}", classification.category());
                }
                if (!priorityProvided) {
                    ticket.setPriority(classification.priority());
                    log.debug("Auto-classified priority: {}", classification.priority());
                }
            } else {
                log.debug("Skipping auto-classification - category and priority already provided");
            }
        }

        Ticket savedTicket = ticketRepository.save(ticket);

        log.info("Ticket created with ID: {}", savedTicket.getId());
        return ticketMapper.toResponse(savedTicket);
    }

    /**
     * Gets a ticket by ID.
     *
     * @param id the ticket ID
     * @return the ticket response
     * @throws TicketNotFoundException if ticket not found
     */
    @Transactional(readOnly = true)
    public TicketResponse getTicket(UUID id) {
        log.debug("Fetching ticket with ID: {}", id);

        Ticket ticket = findTicketById(id);
        return ticketMapper.toResponse(ticket);
    }

    /**
     * Gets a ticket entity by ID.
     *
     * @param id the ticket ID
     * @return the ticket entity
     * @throws TicketNotFoundException if ticket not found
     */
    @Transactional(readOnly = true)
    public Ticket getTicketEntity(UUID id) {
        return findTicketById(id);
    }

    /**
     * Lists all tickets with optional filtering.
     *
     * @param status     optional status filter
     * @param category   optional category filter
     * @param priority   optional priority filter
     * @param customerId optional customer ID filter
     * @return list of ticket responses
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets(Status status, Category category,
                                               Priority priority, String customerId) {
        log.debug("Fetching tickets with filters - status: {}, category: {}, priority: {}, customerId: {}",
            status, category, priority, customerId);

        List<Ticket> tickets;

        if (status == null && category == null && priority == null &&
            (customerId == null || customerId.isBlank())) {
            tickets = ticketRepository.findAll();
        } else {
            tickets = ticketRepository.findByFilters(status, category, priority, customerId);
        }

        log.debug("Found {} tickets", tickets.size());
        return tickets.stream()
            .map(ticketMapper::toResponse)
            .toList();
    }

    /**
     * Updates an existing ticket.
     *
     * @param id      the ticket ID
     * @param request the update request
     * @return the updated ticket response
     * @throws TicketNotFoundException if ticket not found
     */
    public TicketResponse updateTicket(UUID id, TicketUpdateRequest request) {
        log.info("Updating ticket with ID: {}", id);

        Ticket ticket = findTicketById(id);
        ticketMapper.updateEntity(ticket, request);
        Ticket updatedTicket = ticketRepository.save(ticket);

        log.info("Ticket updated: {}", id);
        return ticketMapper.toResponse(updatedTicket);
    }

    /**
     * Deletes a ticket.
     *
     * @param id the ticket ID
     * @throws TicketNotFoundException if ticket not found
     */
    public void deleteTicket(UUID id) {
        log.info("Deleting ticket with ID: {}", id);

        if (!ticketRepository.existsById(id)) {
            throw new TicketNotFoundException(id);
        }

        ticketRepository.deleteById(id);
        log.info("Ticket deleted: {}", id);
    }

    /**
     * Saves a ticket entity.
     *
     * @param ticket the ticket to save
     * @return the saved ticket
     */
    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    private Ticket findTicketById(UUID id) {
        return ticketRepository.findById(id)
            .orElseThrow(() -> new TicketNotFoundException(id));
    }
}
