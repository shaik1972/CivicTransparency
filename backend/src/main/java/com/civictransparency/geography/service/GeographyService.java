package com.civictransparency.geography.service;

import com.civictransparency.geography.dto.CityResponse;
import com.civictransparency.geography.dto.LocalityResponse;
import com.civictransparency.geography.dto.StateResponse;
import com.civictransparency.geography.dto.WardResponse;

import java.util.List;
import java.util.UUID;

public interface GeographyService {

    List<StateResponse> getActiveStates();

    List<CityResponse> getActiveCities(UUID stateId);

    List<WardResponse> getActiveWards(UUID cityId);

    List<LocalityResponse> getActiveLocalities(UUID wardId);
}
