package com.support.ticketsystem.parser;

import com.support.ticketsystem.domain.dto.TicketImportDto;
import com.support.ticketsystem.exception.ImportParseException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Interface for parsing ticket import files.
 */
public interface TicketFileParser {

    /**
     * Parses the uploaded file and returns a list of ticket import DTOs.
     *
     * @param file the uploaded file
     * @return list of parsed ticket DTOs
     * @throws ImportParseException if parsing fails
     */
    List<TicketImportDto> parse(MultipartFile file) throws ImportParseException;

    /**
     * Checks if this parser supports the given content type.
     *
     * @param contentType the file content type
     * @param filename    the file name
     * @return true if supported
     */
    boolean supports(String contentType, String filename);

    /**
     * Returns the file type name for error messages.
     *
     * @return the file type name (e.g., "CSV", "JSON", "XML")
     */
    String getFileTypeName();
}
