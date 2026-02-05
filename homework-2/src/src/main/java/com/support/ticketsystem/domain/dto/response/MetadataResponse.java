package com.support.ticketsystem.domain.dto.response;

import com.support.ticketsystem.domain.enums.DeviceType;
import com.support.ticketsystem.domain.enums.Source;

/**
 * DTO for ticket metadata in responses.
 */
public record MetadataResponse(
    Source source,
    String browser,
    DeviceType deviceType
) {
}
