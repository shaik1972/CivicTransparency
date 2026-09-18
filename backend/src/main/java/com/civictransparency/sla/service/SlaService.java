package com.civictransparency.sla.service;

import com.civictransparency.complaint.entity.Complaint;
import com.civictransparency.sla.entity.ComplaintSla;
import com.civictransparency.sla.entity.SlaPolicy;
import com.civictransparency.sla.repository.ComplaintSlaRepository;
import com.civictransparency.sla.repository.SlaPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class SlaService {

    private final SlaPolicyRepository slaPolicyRepository;
    private final ComplaintSlaRepository complaintSlaRepository;

    public SlaService(SlaPolicyRepository slaPolicyRepository,
            ComplaintSlaRepository complaintSlaRepository) {
        this.slaPolicyRepository = slaPolicyRepository;
        this.complaintSlaRepository = complaintSlaRepository;
    }

    @Transactional
    public void initializeSla(Complaint complaint) {
        if (complaintSlaRepository.findByComplaint_ComplaintId(complaint.getComplaintId()).isPresent()) {
            return;
        }

        UUID categoryId = complaint.getCategory().getCategoryId();
        UUID subcategoryId = complaint.getSubcategory() != null
                ? complaint.getSubcategory().getSubcategoryId()
                : null;
        String severity = complaint.getSeverity().name();

        slaPolicyRepository.findBestMatch(categoryId, subcategoryId, severity).ifPresent(policy -> {
            OffsetDateTime deadline = OffsetDateTime.now().plusHours(policy.getTargetHours());
            ComplaintSla sla = new ComplaintSla(UUID.randomUUID(), complaint, policy, deadline);
            complaintSlaRepository.save(sla);
        });
    }
}
