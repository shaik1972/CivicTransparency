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
import com.civictransparency.workflow.service.WorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
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
    private final WorkflowService workflowService;

    public RoutingEngineService(RoutingRuleRepository routingRuleRepository,
            RoutingDecisionRepository routingDecisionRepository,
            AssignmentService assignmentService,
            SlaService slaService,
            NotificationService notificationService,
            AuditService auditService,
            WorkflowService workflowService) {
        this.routingRuleRepository = routingRuleRepository;
        this.routingDecisionRepository = routingDecisionRepository;
        this.assignmentService = assignmentService;
        this.slaService = slaService;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.workflowService = workflowService;
    }

    @Transactional
    public void routeComplaint(Complaint complaint) {
        UUID stateId = complaint.getCity().getState().getStateId();
        UUID cityId = complaint.getCity().getCityId();
        UUID wardId = complaint.getWard() != null ? complaint.getWard().getWardId() : null;
        UUID categoryId = complaint.getCategory().getCategoryId();
        UUID subcategoryId = complaint.getSubcategory() != null ? complaint.getSubcategory().getSubcategoryId() : null;

        List<RoutingRule> rules = routingRuleRepository.findMatchingActiveRules(
                stateId, cityId, wardId, categoryId, subcategoryId, complaint.getSeverity());

        if (rules.isEmpty()) {
            slaService.initializeSla(complaint);
            auditService.log(
                    "COMPLAINT_ROUTING_UNRESOLVED",
                    null,
                    complaint.getComplaintId(),
                    "No active routing rule matched complaint " + complaint.getComplaintReference());
            return;
        }

        RoutingRule bestRule = rules.stream()
                .max(Comparator.comparingInt((RoutingRule rule) -> getSpecificityScore(rule))
                        .thenComparingInt(rule -> rule.getPriority())
                        .thenComparing(rule -> rule.getRuleId()))
                .orElseThrow();

        int specificityScore = getSpecificityScore(bestRule);

        RoutingDecision decision = new RoutingDecision(
                UUID.randomUUID(),
                complaint,
                bestRule,
                bestRule.getDepartment(),
                specificityScore);

        routingDecisionRepository.save(decision);

        workflowService.markRouted(complaint);
        assignmentService.assignComplaint(complaint, bestRule.getDepartment());
        workflowService.markAssigned(complaint, "AUTOMATED_ROUTING", null);

        slaService.initializeSla(complaint);

        notificationService.notifyComplaintRouted(complaint, bestRule.getDepartment());
        notificationService.notifyDepartmentAssigned(complaint, bestRule.getDepartment());

        auditService.log(
                "COMPLAINT_ROUTED",
                null,
                complaint.getComplaintId(),
                "Complaint routed to department: " + bestRule.getDepartment().getName(),
                "{\"ruleId\":\"" + bestRule.getRuleId() + "\",\"departmentId\":\""
                        + bestRule.getDepartment().getDepartmentId() + "\"}");
    }

    private int getSpecificityScore(RoutingRule rule) {
        int score = 0;
        if (rule.getState() != null)
            score++;
        if (rule.getCity() != null)
            score++;
        if (rule.getWard() != null)
            score++;
        if (rule.getCategory() != null)
            score++;
        if (rule.getSubcategory() != null)
            score++;
        if (rule.getSeverity() != null)
            score++;
        return score;
    }
}
