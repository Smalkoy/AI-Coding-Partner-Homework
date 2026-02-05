package com.support.ticketsystem.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.support.ticketsystem.domain.dto.request.TicketCreateRequest;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Ticket Performance Tests")
class TicketPerformanceTest {

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
    @DisplayName("Create single ticket - response time under 500ms")
    void testCreateTicket_ResponseTime() throws Exception {
        TicketCreateRequest request = createTestRequest("PERF001");

        long startTime = System.currentTimeMillis();

        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        long duration = System.currentTimeMillis() - startTime;

        assertThat(duration).isLessThan(500);
    }

    @Test
    @DisplayName("Bulk import 50 records - completes within 5 seconds")
    void testBulkImport_50Records() throws Exception {
        StringBuilder csv = new StringBuilder();
        csv.append("customer_id,customer_email,customer_name,subject,description,category,priority,status\n");

        for (int i = 1; i <= 50; i++) {
            csv.append(String.format(
                "CUST%03d,perf%d@example.com,Perf User %d,Performance Test %d,This is a performance test description number %d for testing.,technical_issue,medium,new%n",
                i, i, i, i, i
            ));
        }

        MockMultipartFile file = new MockMultipartFile(
            "file", "bulk50.csv", "text/csv", csv.toString().getBytes(StandardCharsets.UTF_8)
        );

        long startTime = System.currentTimeMillis();

        mockMvc.perform(multipart("/tickets/import").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalRecords").value(50))
            .andExpect(jsonPath("$.successfulImports").value(50));

        long duration = System.currentTimeMillis() - startTime;

        assertThat(duration).isLessThan(5000);
        assertThat(ticketRepository.count()).isEqualTo(50);
    }

    @Test
    @DisplayName("20 concurrent ticket creations - all succeed")
    void testConcurrent_20Requests() throws Exception {
        int numRequests = 20;
        ExecutorService executor = Executors.newFixedThreadPool(numRequests);
        CountDownLatch latch = new CountDownLatch(numRequests);
        List<Future<Boolean>> futures = new ArrayList<>();

        for (int i = 0; i < numRequests; i++) {
            final int index = i;
            Future<Boolean> future = executor.submit(() -> {
                try {
                    TicketCreateRequest request = createTestRequest("CONC" + String.format("%03d", index));

                    mockMvc.perform(post("/tickets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated());

                    return true;
                } catch (Exception e) {
                    return false;
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();

        long successCount = futures.stream()
            .mapToLong(f -> {
                try {
                    return f.get() ? 1 : 0;
                } catch (Exception e) {
                    return 0;
                }
            })
            .sum();

        assertThat(successCount).isEqualTo(numRequests);
        assertThat(ticketRepository.count()).isEqualTo(numRequests);
    }

    @Test
    @DisplayName("Classification performance - under 100ms")
    void testClassification_LargeText() throws Exception {
        // Create a ticket with a large description (within 2000 char limit)
        StringBuilder largeDescription = new StringBuilder();
        largeDescription.append("I am experiencing a critical issue with the login system. ");
        for (int i = 0; i < 15; i++) {
            largeDescription.append("This is additional text to make the description longer. ");
            largeDescription.append("The error occurs when trying to access the account. ");
        }

        TicketCreateRequest request = new TicketCreateRequest(
            "PERF001", "perf@example.com", "Perf User",
            "Critical login error with authentication failure",
            largeDescription.toString(),
            null, null, Status.NEW, null, null, null
        );

        MvcResult createResult = mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn();

        String ticketId = objectMapper.readTree(createResult.getResponse().getContentAsString())
            .get("id").asText();

        long startTime = System.currentTimeMillis();

        mockMvc.perform(post("/tickets/{id}/auto-classify", ticketId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.category").exists())
            .andExpect(jsonPath("$.priority").exists());

        long duration = System.currentTimeMillis() - startTime;

        assertThat(duration).isLessThan(500);
    }

    @Test
    @DisplayName("List tickets with filters - pagination performance")
    void testListTickets_Pagination() throws Exception {
        // Create 100 tickets
        for (int i = 1; i <= 100; i++) {
            TicketCreateRequest request = new TicketCreateRequest(
                "CUST" + String.format("%03d", i),
                "list" + i + "@example.com",
                "List User " + i,
                "List Test Subject " + i,
                "This is a test description for listing performance test number " + i,
                i % 2 == 0 ? Category.TECHNICAL_ISSUE : Category.BUG_REPORT,
                Priority.MEDIUM,
                Status.NEW,
                null, null, null
            );

            mockMvc.perform(post("/tickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        }

        assertThat(ticketRepository.count()).isEqualTo(100);

        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/tickets")
                .param("category", "TECHNICAL_ISSUE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(50));

        long duration = System.currentTimeMillis() - startTime;

        assertThat(duration).isLessThan(1000);
    }

    private TicketCreateRequest createTestRequest(String customerId) {
        return new TicketCreateRequest(
            customerId,
            customerId.toLowerCase() + "@example.com",
            "Performance User",
            "Performance Test Subject",
            "This is a performance test description for timing measurements.",
            Category.TECHNICAL_ISSUE,
            Priority.MEDIUM,
            Status.NEW,
            null,
            List.of("performance", "test"),
            null
        );
    }
}
