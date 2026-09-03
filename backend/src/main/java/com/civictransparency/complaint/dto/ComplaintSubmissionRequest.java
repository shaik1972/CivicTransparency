package com.civictransparency.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ComplaintSubmissionRequest(
        @NotNull UUID citizenId,
        @NotNull UUID categoryId,
        UUID subcategoryId,
        String title,
        @NotBlank String description,
        @NotNull UUID cityId,
        UUID wardId,
        
        // Location data
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal accuracyMeters,
        String locationDescription,
        
        // Evidence mock data for now
        List<EvidenceSubmission> evidence
) {
    public record EvidenceSubmission(
            String storageKey,
            String mediaType,
            Long fileSizeBytes,
            String sha256
    ) {}
}
