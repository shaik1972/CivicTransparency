package com.civictransparency.citizen.repository;

import com.civictransparency.citizen.entity.CitizenProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CitizenProfileRepository
        extends JpaRepository<CitizenProfile, UUID> {

    Optional<CitizenProfile> findByUserUserId(UUID userId);

    boolean existsByUserUserId(UUID userId);
}
