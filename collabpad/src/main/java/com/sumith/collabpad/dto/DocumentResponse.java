package com.sumith.collabpad.dto;

import java.time.OffsetDateTime;

public record DocumentResponse(
    Long id,
    String title,
    String content,
    Long parentId,
    String icon,
    String coverImageUrl,
    Double positionOrder,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    Long workspaceId
) {}
