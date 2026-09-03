package com.civictransparency.citizen.service;

import com.civictransparency.citizen.dto.CitizenProfileResponse;
import com.civictransparency.citizen.dto.CreateCitizenProfileRequest;
import com.civictransparency.citizen.entity.CitizenProfile;
import com.civictransparency.citizen.repository.CitizenProfileRepository;
import com.civictransparency.identity.entity.UserAccount;
import com.civictransparency.identity.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CitizenProfileServiceImpl
        implements CitizenProfileService {

    private final CitizenProfileRepository citizenProfileRepository;
    private final UserAccountRepository userAccountRepository;

    public CitizenProfileServiceImpl(
            CitizenProfileRepository citizenProfileRepository,
            UserAccountRepository userAccountRepository) {

        this.citizenProfileRepository = citizenProfileRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public CitizenProfileResponse createProfile(
            UUID userId,
            CreateCitizenProfileRequest request) {

        if (citizenProfileRepository
                .existsByUserUserId(userId)) {

            throw new IllegalArgumentException(
                    "Citizen profile already exists for user: "
                            + userId);
        }

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User account not found: "
                                + userId));

        CitizenProfile profile = new CitizenProfile(
                user,
                request.profileStatus());

        profile.setPublicAlias(request.publicAlias());

        CitizenProfile saved = citizenProfileRepository.save(profile);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CitizenProfileResponse getProfile(
            UUID citizenId) {

        CitizenProfile profile = citizenProfileRepository.findById(citizenId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Citizen profile not found: "
                                + citizenId));

        return toResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public CitizenProfileResponse getProfileByUserId(
            UUID userId) {

        CitizenProfile profile = citizenProfileRepository
                .findByUserUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Citizen profile not found for user: "
                                + userId));

        return toResponse(profile);
    }

    @Override
    public CitizenProfileResponse updateProfileStatus(
            UUID citizenId,
            String profileStatus) {

        CitizenProfile profile = citizenProfileRepository.findById(citizenId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Citizen profile not found: "
                                + citizenId));

        profile.setProfileStatus(profileStatus);

        return toResponse(
                citizenProfileRepository.save(profile));
    }

    private CitizenProfileResponse toResponse(
            CitizenProfile profile) {

        return new CitizenProfileResponse(
                profile.getCitizenId(),
                profile.getUser().getUserId(),
                profile.getPublicAlias(),
                profile.getProfileStatus());
    }
}