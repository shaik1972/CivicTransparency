package com.civictransparency.complaint.repository;

import com.civictransparency.complaint.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, UUID> {
    Optional<Complaint> findByComplaintReference(String reference);
}
