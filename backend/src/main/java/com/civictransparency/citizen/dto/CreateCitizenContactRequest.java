package com.civictransparency.citizen.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCitizenContactRequest(

        @NotBlank String encryptedPhone,

        @NotBlank String encryptedEmail) {
}