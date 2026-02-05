package com.support.ticketsystem.mapper;

import com.support.ticketsystem.domain.dto.request.MetadataDto;
import com.support.ticketsystem.domain.dto.request.TicketCreateRequest;
import com.support.ticketsystem.domain.dto.request.TicketUpdateRequest;
import com.support.ticketsystem.domain.dto.response.MetadataResponse;
import com.support.ticketsystem.domain.dto.response.TicketResponse;
import com.support.ticketsystem.domain.entity.Ticket;
import com.support.ticketsystem.domain.enums.Status;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;

/**
 * Mapper for converting between Ticket entities and DTOs.
 */
@Component
public class TicketMapper {

    /**
     * Converts a TicketCreateRequest to a Ticket entity.
     *
     * @param request the create request
     * @return the Ticket entity
     */
    public Ticket toEntity(TicketCreateRequest request) {
        Ticket.Builder builder = Ticket.builder()
            .customerId(request.customerId())
            .customerEmail(request.customerEmail())
            .customerName(request.customerName())
            .subject(request.subject())
            .description(request.description())
            .category(request.category())
            .priority(request.priority())
            .status(request.status())
            .assignedTo(request.assignedTo())
            .tags(request.tags() != null ? new ArrayList<>(request.tags()) : new ArrayList<>());

        if (request.metadata() != null) {
            builder.metadataSource(request.metadata().source())
                   .metadataBrowser(request.metadata().browser())
                   .metadataDeviceType(request.metadata().deviceType());
        }

        return builder.build();
    }

    /**
     * Converts a Ticket entity to a TicketResponse.
     *
     * @param ticket the ticket entity
     * @return the ticket response DTO
     */
    public TicketResponse toResponse(Ticket ticket) {
        MetadataResponse metadata = new MetadataResponse(
            ticket.getMetadataSource(),
            ticket.getMetadataBrowser(),
            ticket.getMetadataDeviceType()
        );

        return new TicketResponse(
            ticket.getId(),
            ticket.getCustomerId(),
            ticket.getCustomerEmail(),
            ticket.getCustomerName(),
            ticket.getSubject(),
            ticket.getDescription(),
            ticket.getCategory(),
            ticket.getPriority(),
            ticket.getStatus(),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt(),
            ticket.getResolvedAt(),
            ticket.getAssignedTo(),
            ticket.getTags() != null ? new ArrayList<>(ticket.getTags()) : new ArrayList<>(),
            metadata
        );
    }

    /**
     * Updates a Ticket entity with values from TicketUpdateRequest.
     * Only non-null values are applied.
     *
     * @param ticket  the ticket to update
     * @param request the update request
     */
    public void updateEntity(Ticket ticket, TicketUpdateRequest request) {
        if (request.subject() != null) {
            ticket.setSubject(request.subject());
        }
        if (request.description() != null) {
            ticket.setDescription(request.description());
        }
        if (request.category() != null) {
            ticket.setCategory(request.category());
        }
        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }
        if (request.status() != null) {
            Status oldStatus = ticket.getStatus();
            ticket.setStatus(request.status());

            // Auto-set resolvedAt when status changes to RESOLVED
            if (request.status() == Status.RESOLVED && oldStatus != Status.RESOLVED) {
                if (ticket.getResolvedAt() == null) {
                    ticket.setResolvedAt(OffsetDateTime.now());
                }
            }
        }
        if (request.assignedTo() != null) {
            ticket.setAssignedTo(request.assignedTo());
        }
        if (request.tags() != null) {
            ticket.setTags(new ArrayList<>(request.tags()));
        }
        if (request.metadata() != null) {
            applyMetadataUpdate(ticket, request.metadata());
        }
    }

    private void applyMetadataUpdate(Ticket ticket, MetadataDto metadata) {
        if (metadata.source() != null) {
            ticket.setMetadataSource(metadata.source());
        }
        if (metadata.browser() != null) {
            ticket.setMetadataBrowser(metadata.browser());
        }
        if (metadata.deviceType() != null) {
            ticket.setMetadataDeviceType(metadata.deviceType());
        }
    }
}
