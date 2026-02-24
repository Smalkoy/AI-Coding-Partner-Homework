package com.support.ticketsystem.parser;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.support.ticketsystem.domain.dto.TicketImportDto;
import com.support.ticketsystem.exception.ImportParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Parser for CSV ticket import files.
 * Expected CSV format with header row containing column names.
 */
@Component
public class CsvTicketParser implements TicketFileParser {

    private static final Logger log = LoggerFactory.getLogger(CsvTicketParser.class);

    private static final String FILE_TYPE = "CSV";
    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of(
        "text/csv",
        "application/csv",
        "text/plain"
    );

    @Override
    public List<TicketImportDto> parse(MultipartFile file) throws ImportParseException {
        log.info("Parsing CSV file: {}", file.getOriginalFilename());

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReaderBuilder(reader).build()) {

            List<String[]> allRows = csvReader.readAll();

            if (allRows.isEmpty()) {
                log.warn("CSV file is empty");
                return Collections.emptyList();
            }

            // First row is header
            String[] headers = allRows.get(0);
            Map<String, Integer> headerIndex = buildHeaderIndex(headers);

            if (allRows.size() == 1) {
                log.info("CSV file contains only headers, no data rows");
                return Collections.emptyList();
            }

            List<TicketImportDto> tickets = new ArrayList<>();

            // Process data rows (skip header)
            for (int i = 1; i < allRows.size(); i++) {
                String[] row = allRows.get(i);

                // Skip empty rows
                if (isEmptyRow(row)) {
                    continue;
                }

                try {
                    TicketImportDto dto = parseRow(row, headerIndex, i);
                    tickets.add(dto);
                } catch (Exception e) {
                    log.warn("Error parsing row {}: {}", i + 1, e.getMessage());
                    throw new ImportParseException(FILE_TYPE,
                        "Error parsing row " + (i + 1) + ": " + e.getMessage(), e);
                }
            }

            log.info("Successfully parsed {} tickets from CSV", tickets.size());
            return tickets;

        } catch (IOException e) {
            log.error("IO error reading CSV file", e);
            throw new ImportParseException(FILE_TYPE, "Failed to read file: " + e.getMessage(), e);
        } catch (CsvException e) {
            log.error("CSV parsing error", e);
            throw new ImportParseException(FILE_TYPE, "Invalid CSV format: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(String contentType, String filename) {
        if (contentType != null && SUPPORTED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return true;
        }
        return filename != null && filename.toLowerCase().endsWith(".csv");
    }

    @Override
    public String getFileTypeName() {
        return FILE_TYPE;
    }

    private Map<String, Integer> buildHeaderIndex(String[] headers) {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            String header = normalizeHeader(headers[i]);
            index.put(header, i);
        }
        return index;
    }

    private String normalizeHeader(String header) {
        if (header == null) {
            return "";
        }
        // Convert to snake_case format for matching
        return header.trim()
            .toLowerCase()
            .replace(" ", "_")
            .replace("-", "_");
    }

    private boolean isEmptyRow(String[] row) {
        if (row == null || row.length == 0) {
            return true;
        }
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private TicketImportDto parseRow(String[] row, Map<String, Integer> headerIndex, int rowIndex) {
        TicketImportDto dto = new TicketImportDto();

        dto.setCustomerId(getValue(row, headerIndex, "customer_id"));
        dto.setCustomerEmail(getValue(row, headerIndex, "customer_email"));
        dto.setCustomerName(getValue(row, headerIndex, "customer_name"));
        dto.setSubject(getValue(row, headerIndex, "subject"));
        dto.setDescription(getValue(row, headerIndex, "description"));
        dto.setCategory(getValue(row, headerIndex, "category"));
        dto.setPriority(getValue(row, headerIndex, "priority"));
        dto.setStatus(getValue(row, headerIndex, "status"));
        dto.setAssignedTo(getValue(row, headerIndex, "assigned_to"));
        dto.setMetadataSource(getValue(row, headerIndex, "metadata_source", "source"));
        dto.setMetadataBrowser(getValue(row, headerIndex, "metadata_browser", "browser"));
        dto.setMetadataDeviceType(getValue(row, headerIndex, "metadata_device_type", "device_type"));

        // Parse tags as comma-separated values
        String tagsValue = getValue(row, headerIndex, "tags");
        if (tagsValue != null && !tagsValue.isEmpty()) {
            List<String> tags = Arrays.stream(tagsValue.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
            dto.setTags(tags);
        }

        return dto;
    }

    private String getValue(String[] row, Map<String, Integer> headerIndex, String... possibleHeaders) {
        for (String header : possibleHeaders) {
            Integer index = headerIndex.get(header);
            if (index != null && index < row.length) {
                String value = row[index];
                if (value != null && !value.trim().isEmpty()) {
                    return value.trim();
                }
            }
        }
        return null;
    }
}
