package com.civictransparency.citizen.service;

import com.civictransparency.citizen.dto.CitizenRegistrationRequest;
import com.civictransparency.citizen.dto.CitizenRegistrationResponse;

public interface CitizenRegistrationService {

    CitizenRegistrationResponse register(
            CitizenRegistrationRequest request);
}
