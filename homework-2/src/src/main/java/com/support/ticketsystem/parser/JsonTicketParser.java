package com.support.ticketsystem.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.support.ticketsystem.domain.dto.TicketImportDto;
import com.support.ticketsystem.exception.ImportParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Parser for JSON ticket import files.
 * Expected format: JSON array of ticket objects.
 */
@Component
public class JsonTicketParser implements TicketFileParser {

    private static final Logger log = LoggerFactory.getLogger(JsonTicketParser.class);

    private static final String FILE_TYPE = "JSON";
    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of(
        "application/json",
        "text/json"
    );

    private final ObjectMapper objectMapper;

    public JsonTicketParser() {
        this.objectMapper = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    @Override
    public List<TicketImportDto> parse(MultipartFile file) throws ImportParseException {
        log.info("Parsing JSON file: {}", file.getOriginalFilename());

        try {
            byte[] content = file.getBytes();

            if (content.length == 0) {
                log.warn("JSON file is empty");
                return Collections.emptyList();
            }

            String jsonContent = new String(content).trim();

            if (jsonContent.isEmpty() || jsonContent.equals("[]")) {
                log.info("JSON file contains empty array");
                return Collections.emptyList();
            }

            // First, check if it's a valid JSON array
            JsonNode rootNode = objectMapper.readTree(jsonContent);

            if (!rootNode.isArray()) {
                throw new ImportParseException(FILE_TYPE,
                    "JSON content must be an array of ticket objects");
            }

            List<TicketImportDto> tickets = new ArrayList<>();

            for (int i = 0; i < rootNode.size(); i++) {
                JsonNode ticketNode = rootNode.get(i);
                try {
                    TicketImportDto dto = parseTicketNode(ticketNode);
                    tickets.add(dto);
                } catch (Exception e) {
                    log.warn("Error parsing ticket at index {}: {}", i, e.getMessage());
                    throw new ImportParseException(FILE_TYPE,
                        "Error parsing ticket at index " + i + ": " + e.getMessage(), e);
                }
            }

            log.info("Successfully parsed {} tickets from JSON", tickets.size());
            return tickets;

        } catch (JsonProcessingException e) {
            log.error("JSON parsing error", e);
            throw new ImportParseException(FILE_TYPE, "Invalid JSON format: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("IO error reading JSON file", e);
            throw new ImportParseException(FILE_TYPE, "Failed to read file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(String contentType, String filename) {
        if (contentType != null && SUPPORTED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return true;
        }
        return filename != null && filename.toLowerCase().endsWith(".json");
    }

    @Override
    public String getFileTypeName() {
        return FILE_TYPE;
    }

    private TicketImportDto parseTicketNode(JsonNode node) throws JsonProcessingException {
        TicketImportDto dto = new TicketImportDto();

        dto.setCustomerId(getTextValue(node, "customer_id", "customerId"));
        dto.setCustomerEmail(getTextValue(node, "customer_email", "customerEmail"));
        dto.setCustomerName(getTextValue(node, "customer_name", "customerName"));
        dto.setSubject(getTextValue(node, "subject"));
        dto.setDescription(getTextValue(node, "description"));
        dto.setCategory(getTextValue(node, "category"));
        dto.setPriority(getTextValue(node, "priority"));
        dto.setStatus(getTextValue(node, "status"));
        dto.setAssignedTo(getTextValue(node, "assigned_to", "assignedTo"));

        // Parse tags array
        JsonNode tagsNode = getNode(node, "tags");
        if (tagsNode != null && tagsNode.isArray()) {
            List<String> tags = new ArrayList<>();
            for (JsonNode tagNode : tagsNode) {
                if (tagNode.isTextual()) {
                    tags.add(tagNode.asText());
                }
            }
            dto.setTags(tags);
        }

        // Parse metadata (can be nested object or flat)
        JsonNode metadataNode = getNode(node, "metadata");
        if (metadataNode != null && metadataNode.isObject()) {
            dto.setMetadataSource(getTextValue(metadataNode, "source"));
            dto.setMetadataBrowser(getTextValue(metadataNode, "browser"));
            dto.setMetadataDeviceType(getTextValue(metadataNode, "device_type", "deviceType"));
        } else {
            // Try flat metadata fields
            dto.setMetadataSource(getTextValue(node, "metadata_source", "metadataSource", "source"));
            dto.setMetadataBrowser(getTextValue(node, "metadata_browser", "metadataBrowser", "browser"));
            dto.setMetadataDeviceType(getTextValue(node, "metadata_device_type", "metadataDeviceType", "device_type", "deviceType"));
        }

        return dto;
    }

    private String getTextValue(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode fieldNode = node.get(fieldName);
            if (fieldNode != null && !fieldNode.isNull()) {
                if (fieldNode.isTextual()) {
                    String value = fieldNode.asText();
                    return value.isEmpty() ? null : value;
                } else {
                    return fieldNode.asText();
                }
            }
        }
        return null;
    }

    private JsonNode getNode(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode fieldNode = node.get(fieldName);
            if (fieldNode != null && !fieldNode.isNull()) {
                return fieldNode;
            }
        }
        return null;
    }
}
