package com.civictransparency.citizen.repository;

import com.civictransparency.citizen.entity.CitizenContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CitizenContactRepository
        extends JpaRepository<CitizenContact, UUID> {

    boolean existsByCitizenProfileCitizenId(UUID citizenId);
}
