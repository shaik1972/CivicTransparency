package com.civictransparency.geography.controller;

import com.civictransparency.geography.dto.CityResponse;
import com.civictransparency.geography.dto.StateResponse;
import com.civictransparency.geography.service.GeographyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/geography")
public class GeographyController {

    private final GeographyService geographyService;

    public GeographyController(
            GeographyService geographyService) {

        this.geographyService = geographyService;
    }

    @GetMapping("/states")
    public List<StateResponse> getStates() {

        return geographyService.getActiveStates();
    }

    @GetMapping("/states/{stateId}/cities")
    public List<CityResponse> getCities(
            @PathVariable UUID stateId) {

        return geographyService.getActiveCities(stateId);
    }
}
