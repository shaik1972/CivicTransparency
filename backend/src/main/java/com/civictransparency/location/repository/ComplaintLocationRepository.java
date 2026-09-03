package com.civictransparency.location.repository;

import com.civictransparency.location.entity.ComplaintLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface ComplaintLocationRepository extends JpaRepository<ComplaintLocation, UUID> {
    Optional<ComplaintLocation> findByComplaintComplaintId(UUID complaintId);
}
