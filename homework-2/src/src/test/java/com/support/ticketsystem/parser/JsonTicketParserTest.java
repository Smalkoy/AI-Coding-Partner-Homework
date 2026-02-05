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

@DisplayName("JsonTicketParser Tests")
class JsonTicketParserTest {

    private JsonTicketParser parser;

    @BeforeEach
    void setUp() {
        parser = new JsonTicketParser();
    }

    @Test
    @DisplayName("Parse valid JSON array successfully")
    void testParse_ValidJsonArray() {
        String json = """
            [
                {
                    "customer_id": "CUST001",
                    "customer_email": "test@example.com",
                    "customer_name": "Test User",
                    "subject": "Test Subject",
                    "description": "This is a valid description for testing.",
                    "category": "technical_issue",
                    "priority": "high"
                }
            ]
            """;

        MockMultipartFile file = createJsonFile(json);

        List<TicketImportDto> result = parser.parse(file);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo("CUST001");
        assertThat(result.get(0).getCustomerEmail()).isEqualTo("test@example.com");
        assertThat(result.get(0).getCategory()).isEqualTo("technical_issue");
    }

    @Test
    @DisplayName("Parse JSON with nested metadata object")
    void testParse_WithNestedMetadata() {
        String json = """
            [
                {
                    "customer_id": "CUST001",
                    "customer_email": "test@example.com",
                    "customer_name": "Test User",
                    "subject": "Test Subject",
                    "description": "Valid description text here.",
                    "metadata": {
                        "source": "web_form",
                        "browser": "Chrome 120",
                        "device_type": "desktop"
                    }
                }
            ]
            """;

        MockMultipartFile file = createJsonFile(json);

        List<TicketImportDto> result = parser.parse(file);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMetadataSource()).isEqualTo("web_form");
        assertThat(result.get(0).getMetadataBrowser()).isEqualTo("Chrome 120");
        assertThat(result.get(0).getMetadataDeviceType()).isEqualTo("desktop");
    }

    @Test
    @DisplayName("Parse JSON with tags array")
    void testParse_WithTagsArray() {
        String json = """
            [
                {
                    "customer_id": "CUST001",
                    "customer_email": "test@example.com",
                    "customer_name": "Test User",
                    "subject": "Test Subject",
                    "description": "Valid description text here.",
                    "tags": ["tag1", "tag2", "tag3"]
                }
            ]
            """;

        MockMultipartFile file = createJsonFile(json);

        List<TicketImportDto> result = parser.parse(file);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTags()).containsExactly("tag1", "tag2", "tag3");
    }

    @Test
    @DisplayName("Parse empty JSON array")
    void testParse_EmptyArray() {
        String json = "[]";

        MockMultipartFile file = createJsonFile(json);

        List<TicketImportDto> result = parser.parse(file);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Reject JSON object (not array)")
    void testParse_RejectSingleObject() {
        String json = """
            {
                "customer_id": "CUST001",
                "customer_email": "test@example.com",
                "customer_name": "Test User",
                "subject": "Test Subject",
                "description": "Valid description."
            }
            """;

        MockMultipartFile file = createJsonFile(json);

        assertThatThrownBy(() -> parser.parse(file))
            .isInstanceOf(ImportParseException.class)
            .hasMessageContaining("must be an array");
    }

    @Test
    @DisplayName("Supports JSON content type")
    void testSupports_JsonContentType() {
        assertThat(parser.supports("application/json", "test.json")).isTrue();
        assertThat(parser.supports(null, "test.json")).isTrue();
        assertThat(parser.supports("text/csv", "test.csv")).isFalse();
    }

    private MockMultipartFile createJsonFile(String content) {
        return new MockMultipartFile(
            "file",
            "test.json",
            "application/json",
            content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
