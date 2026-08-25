package com.sumith.collabpad.dto;

import java.time.OffsetDateTime;

public record DocumentResponse(
    Long id,
    String title,
    String content,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
