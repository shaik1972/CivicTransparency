package com.civictransparency.sla.service;

import com.civictransparency.complaint.entity.Complaint;
import com.civictransparency.sla.entity.ComplaintSla;
import com.civictransparency.sla.repository.ComplaintSlaRepository;
import com.civictransparency.sla.repository.SlaPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;
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

        slaPolicyRepository.findBestMatches(categoryId, subcategoryId, severity).stream()
                .findFirst()
                .ifPresent(policy -> {
                    OffsetDateTime deadline = OffsetDateTime.now().plusHours(policy.getTargetHours());
                    ComplaintSla sla = new ComplaintSla(UUID.randomUUID(), complaint, policy, deadline);
                    complaintSlaRepository.save(sla);
                });
    }

    @Transactional
    public void pause(UUID complaintId, String reason) {
        ComplaintSla sla = getRequiredSla(complaintId);
        try {
            sla.pause(reason);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
        complaintSlaRepository.save(sla);
    }

    @Transactional
    public void resume(UUID complaintId) {
        ComplaintSla sla = getRequiredSla(complaintId);
        try {
            sla.resume();
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
        complaintSlaRepository.save(sla);
    }

    @Transactional
    public void resolve(UUID complaintId) {
        ComplaintSla sla = getRequiredSla(complaintId);
        try {
            sla.resolve();
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        }
        complaintSlaRepository.save(sla);
    }

    @Transactional
    public void pauseForHold(Complaint complaint) {
        Optional<ComplaintSla> optionalSla = complaintSlaRepository
                .findByComplaint_ComplaintId(complaint.getComplaintId());
        if (optionalSla.isPresent()) {
            ComplaintSla sla = optionalSla.get();
            if (Boolean.TRUE.equals(sla.getPolicy().getHoldPausesSla())
                    && "ACTIVE".equals(sla.getStatus())) {
                sla.pause("Complaint placed on hold");
                complaintSlaRepository.save(sla);
            }
        }
    }

    @Transactional
    public void resumeForWork(Complaint complaint) {
        complaintSlaRepository.findByComplaint_ComplaintId(complaint.getComplaintId())
                .filter(sla -> Boolean.TRUE.equals(sla.getIsPaused()))
                .ifPresent(sla -> {
                    sla.resume();
                    complaintSlaRepository.save(sla);
                });
    }

    @Transactional
    public void resolveForWorkflow(Complaint complaint) {
        complaintSlaRepository.findByComplaint_ComplaintId(complaint.getComplaintId())
                .filter(sla -> !"RESOLVED".equals(sla.getStatus()))
                .ifPresent(sla -> {
                    sla.resolve();
                    complaintSlaRepository.save(sla);
                });
    }

    @Transactional(readOnly = true)
    public Optional<ComplaintSla> findForComplaint(UUID complaintId) {
        return complaintSlaRepository.findByComplaint_ComplaintId(complaintId);
    }

    private ComplaintSla getRequiredSla(UUID complaintId) {
        return complaintSlaRepository.findByComplaint_ComplaintId(complaintId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No SLA exists for this complaint"));
    }
}
