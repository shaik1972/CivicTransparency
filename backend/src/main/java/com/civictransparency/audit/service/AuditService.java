package com.civictransparency.audit.service;

import com.civictransparency.audit.entity.AuditEvent;
import com.civictransparency.audit.repository.AuditEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    /**
     * Log an audit event. Uses REQUIRES_NEW so audit is persisted even if
     * the calling transaction rolls back (provides full audit trail of attempts).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String eventType, UUID actorUserId, UUID complaintId, String description) {
        log(eventType, actorUserId, complaintId, description, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String eventType, UUID actorUserId, UUID complaintId, String description, String payload) {
        AuditEvent event = new AuditEvent(
            UUID.randomUUID(),
            eventType,
            actorUserId,
            complaintId,
            description,
            payload
        );
        auditEventRepository.save(event);
    }
}
