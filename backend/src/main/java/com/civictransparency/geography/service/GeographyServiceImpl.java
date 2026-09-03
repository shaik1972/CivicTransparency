package com.civictransparency.geography.service;

import com.civictransparency.geography.dto.CityResponse;
import com.civictransparency.geography.dto.LocalityResponse;
import com.civictransparency.geography.dto.StateResponse;
import com.civictransparency.geography.dto.WardResponse;
import com.civictransparency.geography.entity.City;
import com.civictransparency.geography.entity.Locality;
import com.civictransparency.geography.entity.State;
import com.civictransparency.geography.entity.Ward;
import com.civictransparency.geography.repository.CityRepository;
import com.civictransparency.geography.repository.LocalityRepository;
import com.civictransparency.geography.repository.StateRepository;
import com.civictransparency.geography.repository.WardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class GeographyServiceImpl implements GeographyService {

    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final WardRepository wardRepository;
    private final LocalityRepository localityRepository;

    public GeographyServiceImpl(
            StateRepository stateRepository,
            CityRepository cityRepository,
            WardRepository wardRepository,
            LocalityRepository localityRepository) {

        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.wardRepository = wardRepository;
        this.localityRepository = localityRepository;
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

    @Override
    @Transactional(readOnly = true)
    public List<WardResponse> getActiveWards(UUID cityId) {

        return wardRepository
                .findByCityCityIdAndActiveTrueOrderByNameAsc(cityId)
                .stream()
                .map(this::toWardResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocalityResponse> getActiveLocalities(UUID wardId) {

        return localityRepository
                .findByWardWardIdAndActiveTrueOrderByNameAsc(wardId)
                .stream()
                .map(this::toLocalityResponse)
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

    private WardResponse toWardResponse(Ward ward) {

        return new WardResponse(
                ward.getWardId(),
                ward.getCity().getCityId(),
                ward.getCode(),
                ward.getName());
    }

    private LocalityResponse toLocalityResponse(Locality locality) {

        return new LocalityResponse(
                locality.getLocalityId(),
                locality.getWard().getWardId(),
                locality.getName());
    }
}
