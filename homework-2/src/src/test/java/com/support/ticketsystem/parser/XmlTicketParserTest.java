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

@DisplayName("XmlTicketParser Tests")
class XmlTicketParserTest {

    private XmlTicketParser parser;

    @BeforeEach
    void setUp() {
        parser = new XmlTicketParser();
    }

    @Test
    @DisplayName("Parse valid XML file successfully")
    void testParse_ValidXml() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tickets>
                <ticket>
                    <customer_id>CUST001</customer_id>
                    <customer_email>test@example.com</customer_email>
                    <customer_name>Test User</customer_name>
                    <subject>Test Subject</subject>
                    <description>This is a valid description for testing.</description>
                    <category>technical_issue</category>
                    <priority>high</priority>
                </ticket>
            </tickets>
            """;

        MockMultipartFile file = createXmlFile(xml);

        List<TicketImportDto> result = parser.parse(file);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo("CUST001");
        assertThat(result.get(0).getCustomerEmail()).isEqualTo("test@example.com");
        assertThat(result.get(0).getCategory()).isEqualTo("technical_issue");
    }

    @Test
    @DisplayName("Parse XML with nested metadata element")
    void testParse_WithNestedMetadata() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tickets>
                <ticket>
                    <customer_id>CUST001</customer_id>
                    <customer_email>test@example.com</customer_email>
                    <customer_name>Test User</customer_name>
                    <subject>Test Subject</subject>
                    <description>Valid description text here.</description>
                    <metadata>
                        <source>web_form</source>
                        <browser>Chrome 120</browser>
                        <device_type>desktop</device_type>
                    </metadata>
                </ticket>
            </tickets>
            """;

        MockMultipartFile file = createXmlFile(xml);

        List<TicketImportDto> result = parser.parse(file);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMetadataSource()).isEqualTo("web_form");
        assertThat(result.get(0).getMetadataBrowser()).isEqualTo("Chrome 120");
        assertThat(result.get(0).getMetadataDeviceType()).isEqualTo("desktop");
    }

    @Test
    @DisplayName("Parse XML with nested tags elements")
    void testParse_WithNestedTags() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tickets>
                <ticket>
                    <customer_id>CUST001</customer_id>
                    <customer_email>test@example.com</customer_email>
                    <customer_name>Test User</customer_name>
                    <subject>Test Subject</subject>
                    <description>Valid description text here.</description>
                    <tags>
                        <tag>tag1</tag>
                        <tag>tag2</tag>
                        <tag>tag3</tag>
                    </tags>
                </ticket>
            </tickets>
            """;

        MockMultipartFile file = createXmlFile(xml);

        List<TicketImportDto> result = parser.parse(file);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTags()).containsExactly("tag1", "tag2", "tag3");
    }

    @Test
    @DisplayName("Parse empty XML with no ticket elements")
    void testParse_EmptyXml() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tickets>
            </tickets>
            """;

        MockMultipartFile file = createXmlFile(xml);

        List<TicketImportDto> result = parser.parse(file);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Supports XML content type")
    void testSupports_XmlContentType() {
        assertThat(parser.supports("application/xml", "test.xml")).isTrue();
        assertThat(parser.supports("text/xml", "test.xml")).isTrue();
        assertThat(parser.supports(null, "test.xml")).isTrue();
        assertThat(parser.supports("application/json", "test.json")).isFalse();
    }

    @Test
    @DisplayName("Parse XML with multiple tickets")
    void testParse_MultipleTickets() {
        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <tickets>
                <ticket>
                    <customer_id>CUST001</customer_id>
                    <customer_email>test1@example.com</customer_email>
                    <customer_name>User One</customer_name>
                    <subject>Subject One</subject>
                    <description>Description for ticket one.</description>
                </ticket>
                <ticket>
                    <customer_id>CUST002</customer_id>
                    <customer_email>test2@example.com</customer_email>
                    <customer_name>User Two</customer_name>
                    <subject>Subject Two</subject>
                    <description>Description for ticket two.</description>
                </ticket>
            </tickets>
            """;

        MockMultipartFile file = createXmlFile(xml);

        List<TicketImportDto> result = parser.parse(file);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCustomerId()).isEqualTo("CUST001");
        assertThat(result.get(1).getCustomerId()).isEqualTo("CUST002");
    }

    private MockMultipartFile createXmlFile(String content) {
        return new MockMultipartFile(
            "file",
            "test.xml",
            "application/xml",
            content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
