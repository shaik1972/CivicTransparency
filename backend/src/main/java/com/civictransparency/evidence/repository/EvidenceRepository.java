package com.civictransparency.evidence.repository;

import com.civictransparency.evidence.entity.Evidence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface EvidenceRepository extends JpaRepository<Evidence, UUID> {
    List<Evidence> findByComplaintComplaintIdOrderByCreatedAtAsc(UUID complaintId);
}
