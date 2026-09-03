package com.civictransparency.citizen.repository;

import com.civictransparency.citizen.entity.CitizenAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CitizenAddressRepository
        extends JpaRepository<CitizenAddress, UUID> {

    List<CitizenAddress> findByCitizenProfileCitizenId(UUID citizenId);

    List<CitizenAddress> findByCitizenProfileCitizenIdAndPrimaryTrue(
            UUID citizenId);

    boolean existsByCitizenProfileCitizenIdAndPrimaryTrue(
            UUID citizenId);
}