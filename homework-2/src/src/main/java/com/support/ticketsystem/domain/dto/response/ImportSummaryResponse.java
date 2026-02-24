package com.support.ticketsystem.domain.dto.response;

import java.util.List;

/**
 * DTO for import operation summary response.
 */
public record ImportSummaryResponse(
    int totalRecords,
    int successfulImports,
    int failedImports,
    List<ImportErrorDetail> errors
) {

    /**
     * Creates a successful import summary with no errors.
     */
    public static ImportSummaryResponse success(int total, int successful) {
        return new ImportSummaryResponse(total, successful, total - successful, List.of());
    }

    /**
     * Creates an import summary with errors.
     */
    public static ImportSummaryResponse withErrors(int total, int successful, List<ImportErrorDetail> errors) {
        return new ImportSummaryResponse(total, successful, total - successful, errors);
    }
}
