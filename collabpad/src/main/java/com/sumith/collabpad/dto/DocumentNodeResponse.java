package com.sumith.collabpad.dto;

import java.util.List;
import java.time.OffsetDateTime;

public record DocumentNodeResponse(
    Long id,
    String title,
    String icon,
    String coverImageUrl,
    Double positionOrder,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<DocumentNodeResponse> children
) {}
