package com.civictransparency.citizen.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CitizenRegistrationRequest(

        @NotBlank
        @Email
        @Size(max = 320)
        String email,

        @Size(max = 100)
        String username,

        @NotBlank
        @Size(min = 8, max = 128)
        String password,

        @NotBlank
        @Size(min = 8, max = 128)
        String confirmPassword,

        @NotBlank
        @Size(max = 100)
        String publicAlias,

        @NotBlank
        @Size(max = 50)
        String phone,

        @NotBlank
        @Size(max = 4000)
        String address,

        UUID cityId,

        @Size(max = 20)
        String postalCode) {
}
