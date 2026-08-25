package com.sumith.collabpad.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data transfer object for creating or updating a document")
public record DocumentRequest(
    @Schema(description = "Title of the document", example = "System Design Document", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    String title,

    @Schema(description = "Markdown content of the document", example = "This document describes the architectural layout...")
    String content
) {}
