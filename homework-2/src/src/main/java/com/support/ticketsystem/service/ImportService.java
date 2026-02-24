package com.support.ticketsystem.service;

import com.support.ticketsystem.domain.dto.TicketImportDto;
import com.support.ticketsystem.domain.dto.response.ImportErrorDetail;
import com.support.ticketsystem.domain.dto.response.ImportSummaryResponse;
import com.support.ticketsystem.domain.entity.Ticket;
import com.support.ticketsystem.domain.enums.*;
import com.support.ticketsystem.exception.ImportParseException;
import com.support.ticketsystem.exception.UnsupportedFileFormatException;
import com.support.ticketsystem.parser.TicketFileParser;
import com.support.ticketsystem.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service for importing tickets from files.
 */
@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final List<TicketFileParser> parsers;
    private final TicketRepository ticketRepository;

    public ImportService(List<TicketFileParser> parsers, TicketRepository ticketRepository) {
        this.parsers = parsers;
        this.ticketRepository = ticketRepository;
    }

    /**
     * Imports tickets from an uploaded file.
     *
     * @param file the uploaded file
     * @return import summary with success/failure counts and error details
     */
    @Transactional
    public ImportSummaryResponse importTickets(MultipartFile file) {
        log.info("Starting import from file: {}, size: {} bytes, content-type: {}",
            file.getOriginalFilename(), file.getSize(), file.getContentType());

        // Find appropriate parser
        TicketFileParser parser = findParser(file);
        log.info("Using {} parser for file import", parser.getFileTypeName());

        // Parse file
        List<TicketImportDto> importDtos;
        try {
            importDtos = parser.parse(file);
        } catch (ImportParseException e) {
            log.error("Failed to parse import file", e);
            throw e;
        }

        if (importDtos.isEmpty()) {
            log.info("No records found in import file");
            return new ImportSummaryResponse(0, 0, 0, List.of());
        }

        log.info("Parsed {} records from file, starting validation and import", importDtos.size());

        // Validate and save each record
        List<ImportErrorDetail> errors = new ArrayList<>();
        int successCount = 0;

        for (int i = 0; i < importDtos.size(); i++) {
            TicketImportDto dto = importDtos.get(i);
            List<ImportErrorDetail> recordErrors = validateAndConvert(dto, i);

            if (recordErrors.isEmpty()) {
                try {
                    Ticket ticket = convertToEntity(dto);
                    ticketRepository.save(ticket);
                    successCount++;
                } catch (Exception e) {
                    log.warn("Failed to save ticket at index {}: {}", i, e.getMessage());
                    errors.add(ImportErrorDetail.recordError(i, "Failed to save: " + e.getMessage()));
                }
            } else {
                errors.addAll(recordErrors);
            }
        }

        log.info("Import complete: {} total, {} successful, {} failed",
            importDtos.size(), successCount, importDtos.size() - successCount);

        return new ImportSummaryResponse(
            importDtos.size(),
            successCount,
            importDtos.size() - successCount,
            errors
        );
    }

    private TicketFileParser findParser(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        for (TicketFileParser parser : parsers) {
            if (parser.supports(contentType, filename)) {
                return parser;
            }
        }

        throw new UnsupportedFileFormatException(
            contentType != null ? contentType : (filename != null ? filename : "unknown")
        );
    }

    private List<ImportErrorDetail> validateAndConvert(TicketImportDto dto, int index) {
        List<ImportErrorDetail> errors = new ArrayList<>();

        // Required field validations
        if (isBlank(dto.getCustomerId())) {
            errors.add(ImportErrorDetail.fieldError(index, "customer_id",
                "customer_id is required", dto.getCustomerId()));
        } else if (dto.getCustomerId().length() > 100) {
            errors.add(ImportErrorDetail.fieldError(index, "customer_id",
                "customer_id must be at most 100 characters", dto.getCustomerId()));
        }

        if (isBlank(dto.getCustomerEmail())) {
            errors.add(ImportErrorDetail.fieldError(index, "customer_email",
                "customer_email is required", dto.getCustomerEmail()));
        } else if (!EMAIL_PATTERN.matcher(dto.getCustomerEmail()).matches()) {
            errors.add(ImportErrorDetail.fieldError(index, "customer_email",
                "customer_email must be a valid email address", dto.getCustomerEmail()));
        }

        if (isBlank(dto.getCustomerName())) {
            errors.add(ImportErrorDetail.fieldError(index, "customer_name",
                "customer_name is required", dto.getCustomerName()));
        } else if (dto.getCustomerName().length() > 200) {
            errors.add(ImportErrorDetail.fieldError(index, "customer_name",
                "customer_name must be at most 200 characters", dto.getCustomerName()));
        }

        if (isBlank(dto.getSubject())) {
            errors.add(ImportErrorDetail.fieldError(index, "subject",
                "subject is required", dto.getSubject()));
        } else if (dto.getSubject().length() > 200) {
            errors.add(ImportErrorDetail.fieldError(index, "subject",
                "subject must be between 1 and 200 characters", dto.getSubject()));
        }

        if (isBlank(dto.getDescription())) {
            errors.add(ImportErrorDetail.fieldError(index, "description",
                "description is required", dto.getDescription()));
        } else if (dto.getDescription().length() < 10) {
            errors.add(ImportErrorDetail.fieldError(index, "description",
                "description must be between 10 and 2000 characters", dto.getDescription()));
        } else if (dto.getDescription().length() > 2000) {
            errors.add(ImportErrorDetail.fieldError(index, "description",
                "description must be between 10 and 2000 characters", truncate(dto.getDescription(), 50)));
        }

        // Optional field validations (only if provided)
        if (!isBlank(dto.getCategory())) {
            try {
                Category.fromValue(dto.getCategory());
            } catch (IllegalArgumentException e) {
                errors.add(ImportErrorDetail.fieldError(index, "category",
                    "category must be one of: account_access, technical_issue, billing_question, feature_request, bug_report, other",
                    dto.getCategory()));
            }
        }

        if (!isBlank(dto.getPriority())) {
            try {
                Priority.fromValue(dto.getPriority());
            } catch (IllegalArgumentException e) {
                errors.add(ImportErrorDetail.fieldError(index, "priority",
                    "priority must be one of: urgent, high, medium, low", dto.getPriority()));
            }
        }

        if (!isBlank(dto.getStatus())) {
            try {
                Status.fromValue(dto.getStatus());
            } catch (IllegalArgumentException e) {
                errors.add(ImportErrorDetail.fieldError(index, "status",
                    "status must be one of: new, in_progress, waiting_customer, resolved, closed",
                    dto.getStatus()));
            }
        }

        if (!isBlank(dto.getMetadataSource())) {
            try {
                Source.fromValue(dto.getMetadataSource());
            } catch (IllegalArgumentException e) {
                errors.add(ImportErrorDetail.fieldError(index, "metadata_source",
                    "source must be one of: web_form, email, api, chat, phone",
                    dto.getMetadataSource()));
            }
        }

        if (!isBlank(dto.getMetadataDeviceType())) {
            try {
                DeviceType.fromValue(dto.getMetadataDeviceType());
            } catch (IllegalArgumentException e) {
                errors.add(ImportErrorDetail.fieldError(index, "metadata_device_type",
                    "device_type must be one of: desktop, mobile, tablet",
                    dto.getMetadataDeviceType()));
            }
        }

        return errors;
    }

    private Ticket convertToEntity(TicketImportDto dto) {
        Ticket.Builder builder = Ticket.builder()
            .customerId(dto.getCustomerId())
            .customerEmail(dto.getCustomerEmail())
            .customerName(dto.getCustomerName())
            .subject(dto.getSubject())
            .description(dto.getDescription());

        if (!isBlank(dto.getCategory())) {
            builder.category(Category.fromValue(dto.getCategory()));
        }

        if (!isBlank(dto.getPriority())) {
            builder.priority(Priority.fromValue(dto.getPriority()));
        }

        if (!isBlank(dto.getStatus())) {
            builder.status(Status.fromValue(dto.getStatus()));
        }

        if (!isBlank(dto.getAssignedTo())) {
            builder.assignedTo(dto.getAssignedTo());
        }

        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            builder.tags(dto.getTags());
        }

        if (!isBlank(dto.getMetadataSource())) {
            builder.metadataSource(Source.fromValue(dto.getMetadataSource()));
        }

        if (!isBlank(dto.getMetadataBrowser())) {
            builder.metadataBrowser(dto.getMetadataBrowser());
        }

        if (!isBlank(dto.getMetadataDeviceType())) {
            builder.metadataDeviceType(DeviceType.fromValue(dto.getMetadataDeviceType()));
        }

        return builder.build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }
}
