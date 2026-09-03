package com.civictransparency.citizen.service;

import com.civictransparency.citizen.dto.CitizenAddressResponse;
import com.civictransparency.citizen.dto.CreateCitizenAddressRequest;
import com.civictransparency.citizen.entity.CitizenAddress;
import com.civictransparency.citizen.entity.CitizenProfile;
import com.civictransparency.citizen.repository.CitizenAddressRepository;
import com.civictransparency.citizen.repository.CitizenProfileRepository;
import com.civictransparency.common.exception.InvalidRegistrationDataException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.civictransparency.geography.repository.CityRepository;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CitizenAddressServiceImpl
                implements CitizenAddressService {

        private final CitizenAddressRepository addressRepository;
        private final CitizenProfileRepository profileRepository;
        private final CityRepository cityRepository;

        public CitizenAddressServiceImpl(
                        CitizenAddressRepository addressRepository,
                        CitizenProfileRepository profileRepository,
                        CityRepository cityRepository) {

                this.addressRepository = addressRepository;
                this.profileRepository = profileRepository;
                this.cityRepository = cityRepository;
        }

        @Override
        public CitizenAddressResponse createAddress(
                        UUID citizenId,
                        CreateCitizenAddressRequest request) {

                CitizenProfile profile = profileRepository.findById(citizenId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Citizen profile not found: "
                                                                + citizenId));

                if (request.primary()
                                && addressRepository
                                                .existsByCitizenProfileCitizenIdAndPrimaryTrue(
                                                                citizenId)) {

                        throw new IllegalArgumentException(
                                        "Citizen already has a primary address");
                }

                if (request.cityId() != null
                                && !cityRepository.existsById(request.cityId())) {

                        throw new InvalidRegistrationDataException();

                }
                CitizenAddress address = new CitizenAddress(
                                profile,
                                request.encryptedAddressText(),
                                request.cityId(),
                                request.postalCode(),
                                request.primary());

                CitizenAddress saved = addressRepository.save(address);

                return toResponse(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public CitizenAddressResponse getAddress(
                        UUID addressId) {

                CitizenAddress address = addressRepository.findById(addressId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Citizen address not found: "
                                                                + addressId));

                return toResponse(address);
        }

        @Override
        @Transactional(readOnly = true)
        public List<CitizenAddressResponse> getAddresses(
                        UUID citizenId) {

                return addressRepository
                                .findByCitizenProfileCitizenId(citizenId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public CitizenAddressResponse getPrimaryAddress(
                        UUID citizenId) {

                List<CitizenAddress> addresses = addressRepository
                                .findByCitizenProfileCitizenIdAndPrimaryTrue(
                                                citizenId);

                if (addresses.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "Primary address not found for citizen: "
                                                        + citizenId);
                }

                return toResponse(addresses.get(0));
        }

        private CitizenAddressResponse toResponse(
                        CitizenAddress address) {

                return new CitizenAddressResponse(
                                address.getAddressId(),
                                address.getCitizenProfile().getCitizenId(),
                                address.getCityId(),
                                address.getPostalCode(),
                                address.isPrimary());
        }
}