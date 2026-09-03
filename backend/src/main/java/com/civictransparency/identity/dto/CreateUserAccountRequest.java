package com.civictransparency.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserAccountRequest(

        @NotBlank @Email @Size(max = 320) String email,

        @Size(max = 100) String username,

        @NotBlank @Size(max = 30) String status) {
}