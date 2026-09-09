package com.sumith.collabpad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DocumentRequest(
        @NotBlank(message = "Title is required") 
        @Size(max = 255, message = "Title cannot exceed 255 characters") 
        String title,
        
        String content,
        
        @NotNull(message = "Workspace ID is required") 
        Long workspaceId,
        
        Long parentId,
        String icon,
        String coverImageUrl,
        Double positionOrder
) {}
