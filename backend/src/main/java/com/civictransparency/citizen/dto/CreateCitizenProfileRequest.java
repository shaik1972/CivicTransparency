package com.civictransparency.citizen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCitizenProfileRequest(

        @NotBlank @Size(max = 100) String publicAlias,

        @NotBlank @Size(max = 30) String profileStatus) {
}