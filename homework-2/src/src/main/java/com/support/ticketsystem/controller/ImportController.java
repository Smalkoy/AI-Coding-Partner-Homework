package com.support.ticketsystem.controller;

import com.support.ticketsystem.domain.dto.response.ImportSummaryResponse;
import com.support.ticketsystem.service.ImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for ticket import operations.
 */
@RestController
@RequestMapping("/tickets")
@Tag(name = "Import", description = "Ticket import endpoints")
public class ImportController {

    private static final Logger log = LoggerFactory.getLogger(ImportController.class);

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    /**
     * Imports tickets from a CSV, JSON, or XML file.
     *
     * @param file the file to import
     * @return import summary with success/failure counts
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Bulk import tickets",
        description = "Import tickets from CSV, JSON, or XML file. " +
                      "Returns a summary with successful and failed import counts."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Import completed (may include partial failures)",
            content = @Content(schema = @Schema(implementation = ImportSummaryResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "File parsing error or malformed file"
        ),
        @ApiResponse(
            responseCode = "415",
            description = "Unsupported file format"
        )
    })
    public ResponseEntity<ImportSummaryResponse> importTickets(
            @Parameter(
                description = "File to import (CSV, JSON, or XML)",
                required = true
            )
            @RequestParam("file") MultipartFile file) {

        log.info("POST /tickets/import - Importing from file: {}, size: {} bytes",
            file.getOriginalFilename(), file.getSize());

        if (file.isEmpty()) {
            log.warn("Empty file uploaded");
            return ResponseEntity.ok(new ImportSummaryResponse(0, 0, 0, java.util.List.of()));
        }

        ImportSummaryResponse summary = importService.importTickets(file);

        log.info("Import completed: {} total, {} successful, {} failed",
            summary.totalRecords(), summary.successfulImports(), summary.failedImports());

        return ResponseEntity.ok(summary);
    }
}
