package com.support.ticketsystem.mapper;

import com.support.ticketsystem.domain.dto.request.MetadataDto;
import com.support.ticketsystem.domain.dto.request.TicketCreateRequest;
import com.support.ticketsystem.domain.dto.request.TicketUpdateRequest;
import com.support.ticketsystem.domain.dto.response.TicketResponse;
import com.support.ticketsystem.domain.entity.Ticket;
import com.support.ticketsystem.domain.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TicketMapper Tests")
class TicketMapperTest {

    private TicketMapper ticketMapper;

    @BeforeEach
    void setUp() {
        ticketMapper = new TicketMapper();
    }

    @Test
    @DisplayName("Convert create request to entity with all fields")
    void testToEntity_AllFields() {
        MetadataDto metadata = new MetadataDto(Source.WEB_FORM, "Chrome", DeviceType.DESKTOP);
        TicketCreateRequest request = new TicketCreateRequest(
            "CUST001", "test@example.com", "Test User",
            "Test Subject", "This is a test description.",
            Category.TECHNICAL_ISSUE, Priority.HIGH, Status.NEW,
            "agent1", List.of("tag1", "tag2"), metadata
        );

        Ticket ticket = ticketMapper.toEntity(request);

        assertThat(ticket.getCustomerId()).isEqualTo("CUST001");
        assertThat(ticket.getCustomerEmail()).isEqualTo("test@example.com");
        assertThat(ticket.getCustomerName()).isEqualTo("Test User");
        assertThat(ticket.getSubject()).isEqualTo("Test Subject");
        assertThat(ticket.getDescription()).isEqualTo("This is a test description.");
        assertThat(ticket.getCategory()).isEqualTo(Category.TECHNICAL_ISSUE);
        assertThat(ticket.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(ticket.getStatus()).isEqualTo(Status.NEW);
        assertThat(ticket.getAssignedTo()).isEqualTo("agent1");
        assertThat(ticket.getTags()).containsExactly("tag1", "tag2");
        assertThat(ticket.getMetadataSource()).isEqualTo(Source.WEB_FORM);
        assertThat(ticket.getMetadataBrowser()).isEqualTo("Chrome");
        assertThat(ticket.getMetadataDeviceType()).isEqualTo(DeviceType.DESKTOP);
    }

    @Test
    @DisplayName("Convert create request to entity without metadata")
    void testToEntity_WithoutMetadata() {
        TicketCreateRequest request = new TicketCreateRequest(
            "CUST001", "test@example.com", "Test User",
            "Test Subject", "This is a test description.",
            Category.BUG_REPORT, Priority.MEDIUM, Status.NEW,
            null, null, null
        );

        Ticket ticket = ticketMapper.toEntity(request);

        assertThat(ticket.getCustomerId()).isEqualTo("CUST001");
        assertThat(ticket.getMetadataSource()).isNull();
        assertThat(ticket.getMetadataBrowser()).isNull();
        assertThat(ticket.getMetadataDeviceType()).isNull();
        assertThat(ticket.getTags()).isEmpty();
    }

    @Test
    @DisplayName("Convert entity to response with all fields")
    void testToResponse_AllFields() {
        Ticket ticket = Ticket.builder()
            .customerId("CUST001")
            .customerEmail("test@example.com")
            .customerName("Test User")
            .subject("Test Subject")
            .description("Test Description")
            .category(Category.FEATURE_REQUEST)
            .priority(Priority.LOW)
            .status(Status.IN_PROGRESS)
            .assignedTo("agent1")
            .tags(List.of("important"))
            .metadataSource(Source.EMAIL)
            .metadataBrowser("Firefox")
            .metadataDeviceType(DeviceType.MOBILE)
            .build();

        setTicketId(ticket, UUID.randomUUID());

        TicketResponse response = ticketMapper.toResponse(ticket);

        assertThat(response.customerId()).isEqualTo("CUST001");
        assertThat(response.customerEmail()).isEqualTo("test@example.com");
        assertThat(response.category()).isEqualTo(Category.FEATURE_REQUEST);
        assertThat(response.metadata().source()).isEqualTo(Source.EMAIL);
        assertThat(response.metadata().browser()).isEqualTo("Firefox");
        assertThat(response.metadata().deviceType()).isEqualTo(DeviceType.MOBILE);
        assertThat(response.tags()).containsExactly("important");
    }

    @Test
    @DisplayName("Convert entity to response with null tags")
    void testToResponse_NullTags() {
        Ticket ticket = Ticket.builder()
            .customerId("CUST001")
            .customerEmail("test@example.com")
            .customerName("Test User")
            .subject("Test Subject")
            .description("Test Description")
            .build();

        setTicketId(ticket, UUID.randomUUID());
        ticket.setTags(null);

        TicketResponse response = ticketMapper.toResponse(ticket);

        assertThat(response.tags()).isEmpty();
    }

    @Test
    @DisplayName("Update entity with all fields")
    void testUpdateEntity_AllFields() {
        Ticket ticket = createBaseTicket();
        MetadataDto metadata = new MetadataDto(Source.API, "Safari", DeviceType.TABLET);
        TicketUpdateRequest request = new TicketUpdateRequest(
            "New Subject", "New Description for update test.",
            Category.BILLING_QUESTION, Priority.URGENT, Status.RESOLVED,
            "newAgent", List.of("updated"), metadata
        );

        ticketMapper.updateEntity(ticket, request);

        assertThat(ticket.getSubject()).isEqualTo("New Subject");
        assertThat(ticket.getDescription()).isEqualTo("New Description for update test.");
        assertThat(ticket.getCategory()).isEqualTo(Category.BILLING_QUESTION);
        assertThat(ticket.getPriority()).isEqualTo(Priority.URGENT);
        assertThat(ticket.getStatus()).isEqualTo(Status.RESOLVED);
        assertThat(ticket.getAssignedTo()).isEqualTo("newAgent");
        assertThat(ticket.getTags()).containsExactly("updated");
        assertThat(ticket.getMetadataSource()).isEqualTo(Source.API);
        assertThat(ticket.getMetadataBrowser()).isEqualTo("Safari");
        assertThat(ticket.getMetadataDeviceType()).isEqualTo(DeviceType.TABLET);
        assertThat(ticket.getResolvedAt()).isNotNull();
    }

    @Test
    @DisplayName("Update entity with partial fields")
    void testUpdateEntity_PartialFields() {
        Ticket ticket = createBaseTicket();
        TicketUpdateRequest request = new TicketUpdateRequest(
            null, null, null, Priority.HIGH, null, null, null, null
        );

        String originalSubject = ticket.getSubject();
        ticketMapper.updateEntity(ticket, request);

        assertThat(ticket.getSubject()).isEqualTo(originalSubject);
        assertThat(ticket.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    @DisplayName("Update entity with partial metadata")
    void testUpdateEntity_PartialMetadata() {
        Ticket ticket = createBaseTicket();
        ticket.setMetadataSource(Source.WEB_FORM);
        ticket.setMetadataBrowser("Chrome");
        ticket.setMetadataDeviceType(DeviceType.DESKTOP);

        MetadataDto metadata = new MetadataDto(Source.CHAT, null, null);
        TicketUpdateRequest request = new TicketUpdateRequest(
            null, null, null, null, null, null, null, metadata
        );

        ticketMapper.updateEntity(ticket, request);

        assertThat(ticket.getMetadataSource()).isEqualTo(Source.CHAT);
        assertThat(ticket.getMetadataBrowser()).isEqualTo("Chrome"); // unchanged
        assertThat(ticket.getMetadataDeviceType()).isEqualTo(DeviceType.DESKTOP); // unchanged
    }

    @Test
    @DisplayName("Update status to resolved sets resolvedAt")
    void testUpdateEntity_StatusResolved_SetsResolvedAt() {
        Ticket ticket = createBaseTicket();
        ticket.setStatus(Status.IN_PROGRESS);
        assertThat(ticket.getResolvedAt()).isNull();

        TicketUpdateRequest request = new TicketUpdateRequest(
            null, null, null, null, Status.RESOLVED, null, null, null
        );

        ticketMapper.updateEntity(ticket, request);

        assertThat(ticket.getStatus()).isEqualTo(Status.RESOLVED);
        assertThat(ticket.getResolvedAt()).isNotNull();
    }

    @Test
    @DisplayName("Update status to resolved does not overwrite existing resolvedAt")
    void testUpdateEntity_StatusResolved_PreservesExistingResolvedAt() {
        Ticket ticket = createBaseTicket();
        ticket.setStatus(Status.RESOLVED);
        ticket.setResolvedAt(java.time.OffsetDateTime.now().minusDays(1));
        var originalResolvedAt = ticket.getResolvedAt();

        TicketUpdateRequest request = new TicketUpdateRequest(
            null, null, null, null, Status.RESOLVED, null, null, null
        );

        ticketMapper.updateEntity(ticket, request);

        assertThat(ticket.getResolvedAt()).isEqualTo(originalResolvedAt);
    }

    private Ticket createBaseTicket() {
        return Ticket.builder()
            .customerId("CUST001")
            .customerEmail("test@example.com")
            .customerName("Test User")
            .subject("Original Subject")
            .description("Original Description")
            .category(Category.OTHER)
            .priority(Priority.MEDIUM)
            .status(Status.NEW)
            .build();
    }

    private void setTicketId(Ticket ticket, UUID id) {
        try {
            var field = Ticket.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(ticket, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ticket ID", e);
        }
    }
}
