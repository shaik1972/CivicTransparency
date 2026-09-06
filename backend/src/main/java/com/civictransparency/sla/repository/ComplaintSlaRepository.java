package com.civictransparency.sla.repository;

import com.civictransparency.sla.entity.ComplaintSla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ComplaintSlaRepository extends JpaRepository<ComplaintSla, UUID> {
    Optional<ComplaintSla> findByComplaint_ComplaintId(UUID complaintId);
}
