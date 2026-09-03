package com.civictransparency.citizen.controller;

import com.civictransparency.citizen.dto.CitizenRegistrationRequest;
import com.civictransparency.citizen.dto.CitizenRegistrationResponse;
import com.civictransparency.citizen.service.CitizenRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/citizens")
public class CitizenRegistrationController {

    private final CitizenRegistrationService registrationService;

    public CitizenRegistrationController(
            CitizenRegistrationService registrationService) {

        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<CitizenRegistrationResponse> register(
            @Valid @RequestBody CitizenRegistrationRequest request) {

        CitizenRegistrationResponse response =
                registrationService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
