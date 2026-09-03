package com.civictransparency.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(

        @NotBlank @Size(max = 50) String code,

        @NotBlank @Size(max = 100) String name) {
}