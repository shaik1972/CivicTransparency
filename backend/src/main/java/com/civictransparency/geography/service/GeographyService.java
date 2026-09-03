package com.civictransparency.geography.service;

import com.civictransparency.geography.dto.CityResponse;
import com.civictransparency.geography.dto.StateResponse;

import java.util.List;
import java.util.UUID;

public interface GeographyService {

    List<StateResponse> getActiveStates();

    List<CityResponse> getActiveCities(UUID stateId);
}
