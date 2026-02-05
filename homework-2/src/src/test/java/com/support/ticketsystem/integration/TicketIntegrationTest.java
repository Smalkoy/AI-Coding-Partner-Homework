package com.support.ticketsystem.integration;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Ticket Integration Tests")
class TicketIntegrationTest {

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
    @DisplayName("Complete ticket lifecycle: create, read, update, delete")
    void testCompleteTicketLifecycle() throws Exception {
        // 1. Create ticket
        TicketCreateRequest createRequest = new TicketCreateRequest(
            "CUST001", "lifecycle@example.com", "Lifecycle User",
            "Lifecycle Test", "Testing the complete ticket lifecycle workflow.",
            Category.TECHNICAL_ISSUE, Priority.MEDIUM, Status.NEW,
            null, List.of("test"), null
        );

        MvcResult createResult = mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.customerId").value("CUST001"))
            .andReturn();

        String ticketId = objectMapper.readTree(createResult.getResponse().getContentAsString())
            .get("id").asText();

        // 2. Read ticket
        mockMvc.perform(get("/tickets/{id}", ticketId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ticketId))
            .andExpect(jsonPath("$.status").value("new"));

        // 3. Update ticket
        TicketUpdateRequest updateRequest = new TicketUpdateRequest(
            null, null, null, Priority.HIGH, Status.IN_PROGRESS, "agent1", null, null
        );

        mockMvc.perform(put("/tickets/{id}", ticketId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("in_progress"))
            .andExpect(jsonPath("$.priority").value("high"))
            .andExpect(jsonPath("$.assignedTo").value("agent1"));

        // 4. Delete ticket
        mockMvc.perform(delete("/tickets/{id}", ticketId))
            .andExpect(status().isNoContent());

        // 5. Verify deletion
        mockMvc.perform(get("/tickets/{id}", ticketId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Bulk import with auto-classification verification")
    void testBulkImportWithAutoClassification() throws Exception {
        String csv = """
            customer_id,customer_email,customer_name,subject,description
            CUST001,import1@example.com,Import User 1,Cannot login to account,I am unable to login after changing password.
            CUST002,import2@example.com,Import User 2,Payment question,I have a question about my invoice charges.
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "import.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/tickets/import").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRecords").value(2))
            .andExpect(jsonPath("$.successfulImports").value(2))
            .andExpect(jsonPath("$.failedImports").value(0));

        // Verify tickets were imported
        assertThat(ticketRepository.count()).isEqualTo(2);

        // Auto-classify one of the imported tickets
        var tickets = ticketRepository.findAll();
        var ticketId = tickets.get(0).getId();

        mockMvc.perform(post("/tickets/{id}/auto-classify", ticketId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.category").exists())
            .andExpect(jsonPath("$.priority").exists())
            .andExpect(jsonPath("$.confidence").exists());
    }

    @Test
    @DisplayName("Combined filtering by category and priority")
    void testFilterByCategoryAndPriority() throws Exception {
        // Create multiple tickets with different categories and priorities
        createTestTicket("CUST001", Category.BUG_REPORT, Priority.HIGH);
        createTestTicket("CUST002", Category.BUG_REPORT, Priority.LOW);
        createTestTicket("CUST003", Category.FEATURE_REQUEST, Priority.HIGH);

        // Filter by category only
        mockMvc.perform(get("/tickets").param("category", "BUG_REPORT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        // Filter by priority only
        mockMvc.perform(get("/tickets").param("priority", "HIGH"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        // Filter by both
        mockMvc.perform(get("/tickets")
                .param("category", "BUG_REPORT")
                .param("priority", "HIGH"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Import with partial failures")
    void testImportPartialFailure() throws Exception {
        String csv = """
            customer_id,customer_email,customer_name,subject,description
            CUST001,valid@example.com,Valid User,Valid Subject,This is a valid description for import.
            ,invalid@example.com,Invalid User,Missing ID,This record has no customer ID.
            CUST003,not-an-email,Invalid Email,Invalid Email,This record has invalid email.
            CUST004,valid2@example.com,Valid User 2,Valid Subject 2,Another valid description here.
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "partial.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/tickets/import").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRecords").value(4))
            .andExpect(jsonPath("$.successfulImports").value(2))
            .andExpect(jsonPath("$.failedImports").value(2))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors.length()").value(2));

        // Verify only valid tickets were saved
        assertThat(ticketRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Create ticket with auto-classify flag")
    void testCreateWithAutoClassify() throws Exception {
        TicketCreateRequest request = new TicketCreateRequest(
            "CUST001", "autoclassify@example.com", "Auto Classify User",
            "Cannot access my account",
            "I have been locked out of my account and cannot login anymore.",
            null, null, Status.NEW, null, null, null
        );

        mockMvc.perform(post("/tickets")
                .param("autoClassify", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.category").value("account_access"));
    }

    private void createTestTicket(String customerId, Category category, Priority priority) throws Exception {
        TicketCreateRequest request = new TicketCreateRequest(
            customerId, customerId.toLowerCase() + "@example.com", "Test User",
            "Test Subject", "This is a test description for integration testing.",
            category, priority, Status.NEW, null, null, null
        );

        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}
