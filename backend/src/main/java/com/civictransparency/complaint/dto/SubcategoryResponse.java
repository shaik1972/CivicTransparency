package com.civictransparency.complaint.dto;

import java.util.UUID;

public record SubcategoryResponse(
        UUID subcategoryId,
        UUID categoryId,
        String code,
        String name,
        String description,
        String defaultPriority) {
}
