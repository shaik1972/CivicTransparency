package com.civictransparency.complaint.dto;

import java.util.List;
import java.util.UUID;

public record CategoryResponse(
        UUID categoryId,
        String code,
        String name,
        String description,
        String iconName,
        int displayOrder,
        List<SubcategoryResponse> subcategories) {
}
