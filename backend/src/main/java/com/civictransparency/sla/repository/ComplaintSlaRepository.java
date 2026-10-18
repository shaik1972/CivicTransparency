package com.civictransparency.sla.repository;

import com.civictransparency.sla.entity.ComplaintSla;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ComplaintSlaRepository extends JpaRepository<ComplaintSla, UUID> {
    Optional<ComplaintSla> findByComplaint_ComplaintId(UUID complaintId);
}
