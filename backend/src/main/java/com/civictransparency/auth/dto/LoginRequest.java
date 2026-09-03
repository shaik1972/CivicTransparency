package com.civictransparency.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank
        @Size(max = 320)
        String identifier,

        @NotBlank
        @Size(max = 200)
        String password) {
}
