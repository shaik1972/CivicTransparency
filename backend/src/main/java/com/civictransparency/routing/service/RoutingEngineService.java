package com.civictransparency.routing.service;

import com.civictransparency.assignment.service.AssignmentService;
import com.civictransparency.audit.service.AuditService;
import com.civictransparency.complaint.entity.Complaint;
import com.civictransparency.notification.service.NotificationService;
import com.civictransparency.routing.entity.RoutingDecision;
import com.civictransparency.routing.entity.RoutingRule;
import com.civictransparency.routing.repository.RoutingDecisionRepository;
import com.civictransparency.routing.repository.RoutingRuleRepository;
import com.civictransparency.sla.service.SlaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoutingEngineService {
    
    private final RoutingRuleRepository routingRuleRepository;
    private final RoutingDecisionRepository routingDecisionRepository;
    private final AssignmentService assignmentService;
    private final SlaService slaService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public RoutingEngineService(RoutingRuleRepository routingRuleRepository,
                                RoutingDecisionRepository routingDecisionRepository,
                                AssignmentService assignmentService,
                                SlaService slaService,
                                NotificationService notificationService,
                                AuditService auditService) {
        this.routingRuleRepository = routingRuleRepository;
        this.routingDecisionRepository = routingDecisionRepository;
        this.assignmentService = assignmentService;
        this.slaService = slaService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    @Transactional
    public void routeComplaint(Complaint complaint) {
        UUID stateId = complaint.getCity().getState().getStateId();
        UUID cityId = complaint.getCity().getCityId();
        UUID wardId = complaint.getWard() != null ? complaint.getWard().getWardId() : null;
        UUID categoryId = complaint.getCategory().getCategoryId();
        UUID subcategoryId = complaint.getSubcategory() != null ? complaint.getSubcategory().getSubcategoryId() : null;

        List<RoutingRule> rules = routingRuleRepository.findMatchingActiveRules(
            stateId, cityId, wardId, categoryId, subcategoryId
        );

        if (rules.isEmpty()) {
            return;
        }

        RoutingRule bestRule = rules.get(0); // Query already orders by priority DESC
        
        RoutingDecision decision = new RoutingDecision(
            UUID.randomUUID(),
            complaint,
            bestRule,
            bestRule.getDepartment(),
            bestRule.getPriority()
        );
        
        routingDecisionRepository.save(decision);

        assignmentService.assignComplaint(complaint, bestRule.getDepartment());

        slaService.initializeSla(complaint);

        notificationService.notifyComplaintRouted(complaint, bestRule.getDepartment());
        notificationService.notifyDepartmentAssigned(complaint, bestRule.getDepartment());

        auditService.log(
            "COMPLAINT_ROUTED",
            null,
            complaint.getComplaintId(),
            "Complaint routed to department: " + bestRule.getDepartment().getName(),
            "{\"ruleId\":\"" + bestRule.getRuleId() + "\",\"departmentId\":\"" + bestRule.getDepartment().getDepartmentId() + "\"}"
        );
    }
}
