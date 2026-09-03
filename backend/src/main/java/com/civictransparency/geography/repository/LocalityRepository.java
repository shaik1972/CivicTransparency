package com.civictransparency.geography.repository;

import com.civictransparency.geography.entity.Locality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LocalityRepository extends JpaRepository<Locality, UUID> {

    List<Locality> findByWardWardIdAndActiveTrueOrderByNameAsc(UUID wardId);
}
