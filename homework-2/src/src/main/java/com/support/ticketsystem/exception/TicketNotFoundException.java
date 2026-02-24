package com.support.ticketsystem.exception;

import java.util.UUID;

/**
 * Exception thrown when a ticket is not found.
 */
public class TicketNotFoundException extends RuntimeException {

    private final UUID ticketId;

    public TicketNotFoundException(UUID ticketId) {
        super("Ticket not found with id: " + ticketId);
        this.ticketId = ticketId;
    }

    public UUID getTicketId() {
        return ticketId;
    }
}
