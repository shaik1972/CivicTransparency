package com.civictransparency.geography.repository;

import com.civictransparency.geography.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WardRepository extends JpaRepository<Ward, UUID> {

    List<Ward> findByCityCityIdAndActiveTrueOrderByNameAsc(UUID cityId);
}
