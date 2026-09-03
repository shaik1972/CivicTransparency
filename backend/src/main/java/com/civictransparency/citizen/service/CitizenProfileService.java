package com.civictransparency.citizen.service;

import com.civictransparency.citizen.dto.CitizenProfileResponse;
import com.civictransparency.citizen.dto.CreateCitizenProfileRequest;

import java.util.UUID;

public interface CitizenProfileService {

    CitizenProfileResponse createProfile(
            UUID userId,
            CreateCitizenProfileRequest request);

    CitizenProfileResponse getProfile(UUID citizenId);

    CitizenProfileResponse getProfileByUserId(UUID userId);

    CitizenProfileResponse updateProfileStatus(
            UUID citizenId,
            String profileStatus);
}