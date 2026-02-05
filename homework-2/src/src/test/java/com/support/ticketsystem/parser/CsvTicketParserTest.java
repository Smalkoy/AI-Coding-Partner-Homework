package com.support.ticketsystem.parser;

import com.support.ticketsystem.domain.dto.TicketImportDto;
import com.support.ticketsystem.exception.ImportParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CsvTicketParser Tests")
class CsvTicketParserTest {

    private CsvTicketParser parser;

    @BeforeEach
    void setUp() {
        parser = new CsvTicketParser();
    }

    @Test
    @DisplayName("Parse valid CSV file successfully")
    void testParse_ValidCsv() {
        String csv = """
            customer_id,customer_email,customer_name,subject,description,category,priority,status
            CUST001,test@example.com,Test User,Test Subject,This is a valid description for testing.,technical_issue,high,new
            CUST002,test2@example.com,Test User 2,Another Subject,Another valid description here.,billing_question,medium,in_progress
            """;

        MockMultipartFile file = createCsvFile(csv);

        List<TicketImportDto> result = parser.parse(file);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCustomerId()).isEqualTo("CUST001");
        assertThat(result.get(0).getCustomerEmail()).isEqualTo("test@example.com");
        assertThat(result.get(0).getCategory()).isEqualTo("technical_issue");
        assertThat(result.get(1).getCustomerId()).isEqualTo("CUST002");
    }

    @Test
    @DisplayName("Parse CSV with tags as comma-separated values")
    void testParse_WithTags() {
        String csv = """
            customer_id,customer_email,customer_name,subject,description,tags
            CUST001,test@example.com,Test User,Subject,Valid description text here.,"tag1,tag2,tag3"
            """;

        MockMultipartFile file = createCsvFile(csv);

        List<TicketImportDto> result = parser.parse(file);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTags()).containsExactly("tag1", "tag2", "tag3");
    }

    @Test
    @DisplayName("Parse empty CSV file (headers only)")
    void testParse_EmptyCsv() {
        String csv = "customer_id,customer_email,customer_name,subject,description\n";

        MockMultipartFile file = createCsvFile(csv);

        List<TicketImportDto> result = parser.parse(file);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Parse CSV skips empty rows")
    void testParse_SkipsEmptyRows() {
        String csv = """
            customer_id,customer_email,customer_name,subject,description
            CUST001,test@example.com,Test User,Subject,Valid description text.

            CUST002,test2@example.com,Test User 2,Subject 2,Another valid description.
            """;

        MockMultipartFile file = createCsvFile(csv);

        List<TicketImportDto> result = parser.parse(file);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Parse CSV with metadata fields")
    void testParse_WithMetadata() {
        String csv = """
            customer_id,customer_email,customer_name,subject,description,metadata_source,metadata_browser,metadata_device_type
            CUST001,test@example.com,Test User,Subject,Valid description text.,web_form,Chrome 120,desktop
            """;

        MockMultipartFile file = createCsvFile(csv);

        List<TicketImportDto> result = parser.parse(file);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMetadataSource()).isEqualTo("web_form");
        assertThat(result.get(0).getMetadataBrowser()).isEqualTo("Chrome 120");
        assertThat(result.get(0).getMetadataDeviceType()).isEqualTo("desktop");
    }

    @Test
    @DisplayName("Supports CSV content type")
    void testSupports_CsvContentType() {
        assertThat(parser.supports("text/csv", "test.csv")).isTrue();
        assertThat(parser.supports("application/csv", "test.csv")).isTrue();
        assertThat(parser.supports(null, "test.csv")).isTrue();
        assertThat(parser.supports("application/json", "test.json")).isFalse();
    }

    @Test
    @DisplayName("Parse CSV handles quoted values with commas")
    void testParse_QuotedValuesWithCommas() {
        String csv = """
            customer_id,customer_email,customer_name,subject,description
            CUST001,test@example.com,"User, Test","Subject, with comma","Description, with multiple, commas here."
            """;

        MockMultipartFile file = createCsvFile(csv);

        List<TicketImportDto> result = parser.parse(file);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerName()).isEqualTo("User, Test");
        assertThat(result.get(0).getSubject()).isEqualTo("Subject, with comma");
    }

    private MockMultipartFile createCsvFile(String content) {
        return new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
