package com.civictransparency.citizen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCitizenAddressRequest(

        @NotBlank @Size(max = 4000) String encryptedAddressText,

        UUID cityId,

        @Size(max = 20) String postalCode,

        boolean primary) {
}