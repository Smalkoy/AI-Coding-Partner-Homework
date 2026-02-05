package com.support.ticketsystem.domain.dto.request;

import com.support.ticketsystem.domain.enums.DeviceType;
import com.support.ticketsystem.domain.enums.Source;
import jakarta.validation.constraints.Size;

/**
 * DTO for ticket metadata in requests.
 */
public record MetadataDto(
    Source source,

    @Size(max = 100, message = "browser must be at most 100 characters")
    String browser,

    DeviceType deviceType
) {
}
