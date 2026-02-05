package com.support.ticketsystem.service;

import com.support.ticketsystem.domain.dto.response.ImportSummaryResponse;
import com.support.ticketsystem.exception.UnsupportedFileFormatException;
import com.support.ticketsystem.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ImportService Tests")
class ImportServiceTest {

    @Autowired
    private ImportService importService;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
    }

    @Test
    @DisplayName("Import CSV with all valid records")
    void testImportCsv_AllValid() {
        String csv = """
            customer_id,customer_email,customer_name,subject,description,category,priority,status
            CUST001,test1@example.com,User One,Test Subject,This is a valid test description.,technical_issue,medium,new
            CUST002,test2@example.com,User Two,Another Subject,Another valid test description here.,bug_report,high,in_progress
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.totalRecords()).isEqualTo(2);
        assertThat(result.successfulImports()).isEqualTo(2);
        assertThat(result.failedImports()).isEqualTo(0);
        assertThat(result.errors()).isEmpty();
        assertThat(ticketRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Import CSV with missing customer_id")
    void testImportCsv_MissingCustomerId() {
        String csv = """
            customer_id,customer_email,customer_name,subject,description
            ,test@example.com,User Name,Test Subject,This is a valid test description.
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.failedImports()).isEqualTo(1);
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0).field()).isEqualTo("customer_id");
    }

    @Test
    @DisplayName("Import CSV with customer_id too long")
    void testImportCsv_CustomerIdTooLong() {
        String longId = "A".repeat(101);
        String csv = String.format("""
            customer_id,customer_email,customer_name,subject,description
            %s,test@example.com,User Name,Test Subject,This is a valid test description.
            """, longId);

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.failedImports()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.field().equals("customer_id") && e.message().contains("100 characters"));
    }

    @Test
    @DisplayName("Import CSV with invalid email")
    void testImportCsv_InvalidEmail() {
        String csv = """
            customer_id,customer_email,customer_name,subject,description
            CUST001,not-an-email,User Name,Test Subject,This is a valid test description.
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.failedImports()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.field().equals("customer_email"));
    }

    @Test
    @DisplayName("Import CSV with customer_name too long")
    void testImportCsv_CustomerNameTooLong() {
        String longName = "N".repeat(201);
        String csv = String.format("""
            customer_id,customer_email,customer_name,subject,description
            CUST001,test@example.com,%s,Test Subject,This is a valid test description.
            """, longName);

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.failedImports()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.field().equals("customer_name") && e.message().contains("200 characters"));
    }

    @Test
    @DisplayName("Import CSV with subject too long")
    void testImportCsv_SubjectTooLong() {
        String longSubject = "S".repeat(201);
        String csv = String.format("""
            customer_id,customer_email,customer_name,subject,description
            CUST001,test@example.com,User Name,%s,This is a valid test description.
            """, longSubject);

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.failedImports()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.field().equals("subject"));
    }

    @Test
    @DisplayName("Import CSV with description too short")
    void testImportCsv_DescriptionTooShort() {
        String csv = """
            customer_id,customer_email,customer_name,subject,description
            CUST001,test@example.com,User Name,Test Subject,Too short
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.failedImports()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.field().equals("description") && e.message().contains("10 and 2000"));
    }

    @Test
    @DisplayName("Import CSV with description too long")
    void testImportCsv_DescriptionTooLong() {
        String longDescription = "D".repeat(2001);
        String csv = String.format("""
            customer_id,customer_email,customer_name,subject,description
            CUST001,test@example.com,User Name,Test Subject,%s
            """, longDescription);

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.failedImports()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.field().equals("description"));
    }

    @Test
    @DisplayName("Import CSV with invalid category")
    void testImportCsv_InvalidCategory() {
        String csv = """
            customer_id,customer_email,customer_name,subject,description,category
            CUST001,test@example.com,User Name,Test Subject,This is a valid test description.,invalid_category
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.failedImports()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.field().equals("category"));
    }

    @Test
    @DisplayName("Import CSV with invalid priority")
    void testImportCsv_InvalidPriority() {
        String csv = """
            customer_id,customer_email,customer_name,subject,description,priority
            CUST001,test@example.com,User Name,Test Subject,This is a valid test description.,invalid_priority
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.failedImports()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.field().equals("priority"));
    }

    @Test
    @DisplayName("Import CSV with invalid status")
    void testImportCsv_InvalidStatus() {
        String csv = """
            customer_id,customer_email,customer_name,subject,description,status
            CUST001,test@example.com,User Name,Test Subject,This is a valid test description.,invalid_status
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.failedImports()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.field().equals("status"));
    }

    @Test
    @DisplayName("Import CSV with invalid metadata source")
    void testImportCsv_InvalidMetadataSource() {
        String csv = """
            customer_id,customer_email,customer_name,subject,description,metadata_source
            CUST001,test@example.com,User Name,Test Subject,This is a valid test description.,invalid_source
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.failedImports()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.field().equals("metadata_source"));
    }

    @Test
    @DisplayName("Import CSV with invalid device type")
    void testImportCsv_InvalidDeviceType() {
        String csv = """
            customer_id,customer_email,customer_name,subject,description,metadata_device_type
            CUST001,test@example.com,User Name,Test Subject,This is a valid test description.,invalid_device
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.failedImports()).isEqualTo(1);
        assertThat(result.errors()).anyMatch(e -> e.field().equals("metadata_device_type"));
    }

    @Test
    @DisplayName("Import CSV with all optional fields")
    void testImportCsv_WithAllOptionalFields() {
        String csv = """
            customer_id,customer_email,customer_name,subject,description,category,priority,status,assigned_to,tags,metadata_source,metadata_browser,metadata_device_type
            CUST001,test@example.com,User Name,Test Subject,This is a valid test description.,technical_issue,high,in_progress,agent1,"tag1,tag2",web_form,Chrome,desktop
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.successfulImports()).isEqualTo(1);
        assertThat(result.failedImports()).isEqualTo(0);
        assertThat(ticketRepository.count()).isEqualTo(1);

        var ticket = ticketRepository.findAll().get(0);
        assertThat(ticket.getAssignedTo()).isEqualTo("agent1");
        assertThat(ticket.getMetadataBrowser()).isEqualTo("Chrome");
    }

    @Test
    @DisplayName("Import empty CSV")
    void testImportCsv_EmptyFile() {
        String csv = "customer_id,customer_email,customer_name,subject,description\n";

        MockMultipartFile file = new MockMultipartFile(
            "file", "empty.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.totalRecords()).isEqualTo(0);
        assertThat(result.successfulImports()).isEqualTo(0);
        assertThat(result.failedImports()).isEqualTo(0);
    }

    @Test
    @DisplayName("Import unsupported file format")
    void testImport_UnsupportedFormat() {
        MockMultipartFile file = new MockMultipartFile(
            "file", "data.pdf", "application/pdf", "some pdf content".getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> importService.importTickets(file))
            .isInstanceOf(UnsupportedFileFormatException.class);
    }

    @Test
    @DisplayName("Import JSON with valid records")
    void testImportJson_Valid() {
        String json = """
            [
                {
                    "customer_id": "CUST001",
                    "customer_email": "json@example.com",
                    "customer_name": "JSON User",
                    "subject": "JSON Import Test",
                    "description": "Testing JSON import functionality."
                }
            ]
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.json", "application/json", json.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.successfulImports()).isEqualTo(1);
        assertThat(ticketRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Import XML with valid records")
    void testImportXml_Valid() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tickets>
                <ticket>
                    <customer_id>CUST001</customer_id>
                    <customer_email>xml@example.com</customer_email>
                    <customer_name>XML User</customer_name>
                    <subject>XML Import Test</subject>
                    <description>Testing XML import functionality.</description>
                </ticket>
            </tickets>
            """;

        MockMultipartFile file = new MockMultipartFile(
            "file", "tickets.xml", "application/xml", xml.getBytes(StandardCharsets.UTF_8)
        );

        ImportSummaryResponse result = importService.importTickets(file);

        assertThat(result.successfulImports()).isEqualTo(1);
        assertThat(ticketRepository.count()).isEqualTo(1);
    }
}
