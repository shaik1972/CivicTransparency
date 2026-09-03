package com.civictransparency.geography.repository;

import com.civictransparency.geography.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CityRepository
        extends JpaRepository<City, UUID> {

    List<City> findByStateStateIdAndActiveTrueOrderByNameAsc(
            UUID stateId);
}
