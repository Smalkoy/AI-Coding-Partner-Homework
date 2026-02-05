package com.support.ticketsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.support.ticketsystem.domain.dto.request.TicketCreateRequest;
import com.support.ticketsystem.domain.dto.request.TicketUpdateRequest;
import com.support.ticketsystem.domain.enums.Category;
import com.support.ticketsystem.domain.enums.Priority;
import com.support.ticketsystem.domain.enums.Status;
import com.support.ticketsystem.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("TicketController Tests")
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /tickets - Create ticket successfully")
    void testCreateTicket_Success() throws Exception {
        TicketCreateRequest request = createValidRequest();

        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.customerId").value("CUST001"))
            .andExpect(jsonPath("$.subject").value("Test Subject"));
    }

    @Test
    @DisplayName("POST /tickets - Validation error for invalid email")
    void testCreateTicket_ValidationError() throws Exception {
        String invalidRequest = """
            {
                "customerId": "CUST001",
                "customerEmail": "invalid-email",
                "customerName": "Test User",
                "subject": "Test Subject",
                "description": "This is a valid description for testing purposes."
            }
            """;

        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("GET /tickets/{id} - Get ticket found")
    void testGetTicket_Found() throws Exception {
        // Create a ticket first
        String ticketId = createTestTicket();

        mockMvc.perform(get("/tickets/{id}", ticketId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ticketId))
            .andExpect(jsonPath("$.subject").value("Test Subject"));
    }

    @Test
    @DisplayName("GET /tickets/{id} - Ticket not found")
    void testGetTicket_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(get("/tickets/{id}", randomId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /tickets - List all tickets without filters")
    void testListTickets_NoFilter() throws Exception {
        createTestTicket();

        mockMvc.perform(get("/tickets"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /tickets - List tickets with category filter")
    void testListTickets_WithCategoryFilter() throws Exception {
        createTestTicket();

        mockMvc.perform(get("/tickets").param("category", "TECHNICAL_ISSUE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /tickets - List tickets with multiple filters")
    void testListTickets_WithMultipleFilters() throws Exception {
        // Create ticket with specific values
        TicketCreateRequest request = new TicketCreateRequest(
            "CUST001", "test@example.com", "Test User",
            "Test Subject", "This is a valid description for testing purposes.",
            Category.BUG_REPORT, Priority.HIGH, Status.NEW,
            null, List.of("test"), null
        );

        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/tickets")
                .param("category", "BUG_REPORT")
                .param("priority", "HIGH")
                .param("status", "NEW"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("PUT /tickets/{id} - Update ticket successfully")
    void testUpdateTicket_Success() throws Exception {
        String ticketId = createTestTicket();

        TicketUpdateRequest updateRequest = new TicketUpdateRequest(
            null, null, null, Priority.HIGH, Status.IN_PROGRESS, null, null, null
        );

        mockMvc.perform(put("/tickets/{id}", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.priority").value("high"))
            .andExpect(jsonPath("$.status").value("in_progress"));
    }

    @Test
    @DisplayName("PUT /tickets/{id} - Update ticket not found")
    void testUpdateTicket_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();
        TicketUpdateRequest updateRequest = new TicketUpdateRequest(
            null, null, null, Priority.HIGH, null, null, null, null
        );

        mockMvc.perform(put("/tickets/{id}", randomId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /tickets/{id} - Delete ticket successfully")
    void testDeleteTicket_Success() throws Exception {
        String ticketId = createTestTicket();

        mockMvc.perform(delete("/tickets/{id}", ticketId))
            .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/tickets/{id}", ticketId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /tickets/{id} - Delete ticket not found")
    void testDeleteTicket_NotFound() throws Exception {
        UUID randomId = UUID.randomUUID();

        mockMvc.perform(delete("/tickets/{id}", randomId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /tickets/{id}/auto-classify - Classify ticket successfully")
    void testAutoClassifyTicket_Success() throws Exception {
        // Create a ticket that can be classified
        TicketCreateRequest request = new TicketCreateRequest(
            "CUST001", "test@example.com", "Test User",
            "Cannot login to my account",
            "I am experiencing login issues and cannot access my account. This is urgent!",
            null, null, Status.NEW, null, null, null
        );

        MvcResult createResult = mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        String ticketId = objectMapper.readTree(createResult.getResponse().getContentAsString())
            .get("id").asText();

        mockMvc.perform(post("/tickets/{id}/auto-classify", ticketId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.category").exists())
            .andExpect(jsonPath("$.priority").exists())
            .andExpect(jsonPath("$.confidence").exists());
    }

    private TicketCreateRequest createValidRequest() {
        return new TicketCreateRequest(
            "CUST001",
            "test@example.com",
            "Test User",
            "Test Subject",
            "This is a valid description for testing purposes.",
            Category.TECHNICAL_ISSUE,
            Priority.MEDIUM,
            Status.NEW,
            null,
            List.of("test"),
            null
        );
    }

    private String createTestTicket() throws Exception {
        TicketCreateRequest request = createValidRequest();

        MvcResult result = mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
            .get("id").asText();
    }
}
