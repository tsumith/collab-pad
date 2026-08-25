package com.sumith.collabpad.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "Data transfer object representing a document's response payload")
public record DocumentResponse(
    @Schema(description = "Unique identifier of the document", example = "1")
    Long id,

    @Schema(description = "Title of the document", example = "System Design Document")
    String title,

    @Schema(description = "Markdown content of the document", example = "This document describes the architectural layout...")
    String content,

    @Schema(description = "Timestamp when the document was created")
    OffsetDateTime createdAt,

    @Schema(description = "Timestamp when the document was last updated")
    OffsetDateTime updatedAt
) {}
