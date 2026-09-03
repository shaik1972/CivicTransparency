package com.civictransparency.citizen.service;

import com.civictransparency.citizen.dto.CitizenAddressResponse;
import com.civictransparency.citizen.dto.CreateCitizenAddressRequest;

import java.util.List;
import java.util.UUID;

public interface CitizenAddressService {

    CitizenAddressResponse createAddress(
            UUID citizenId,
            CreateCitizenAddressRequest request);

    CitizenAddressResponse getAddress(UUID addressId);

    List<CitizenAddressResponse> getAddresses(
            UUID citizenId);

    CitizenAddressResponse getPrimaryAddress(
            UUID citizenId);
}
