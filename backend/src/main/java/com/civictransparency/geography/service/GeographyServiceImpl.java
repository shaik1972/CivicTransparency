package com.civictransparency.geography.service;

import com.civictransparency.geography.dto.CityResponse;
import com.civictransparency.geography.dto.StateResponse;
import com.civictransparency.geography.entity.City;
import com.civictransparency.geography.entity.State;
import com.civictransparency.geography.repository.CityRepository;
import com.civictransparency.geography.repository.StateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GeographyServiceImpl implements GeographyService {

    private final StateRepository stateRepository;
    private final CityRepository cityRepository;

    public GeographyServiceImpl(
            StateRepository stateRepository,
            CityRepository cityRepository) {

        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StateResponse> getActiveStates() {

        return stateRepository
                .findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toStateResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CityResponse> getActiveCities(UUID stateId) {

        return cityRepository
                .findByStateStateIdAndActiveTrueOrderByNameAsc(stateId)
                .stream()
                .map(this::toCityResponse)
                .toList();
    }

    private StateResponse toStateResponse(State state) {

        return new StateResponse(
                state.getStateId(),
                state.getCode(),
                state.getName());
    }

    private CityResponse toCityResponse(City city) {

        return new CityResponse(
                city.getCityId(),
                city.getState().getStateId(),
                city.getCode(),
                city.getName());
    }
}
