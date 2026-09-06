package com.civictransparency.routing.service;

import com.civictransparency.assignment.service.AssignmentService;
import com.civictransparency.complaint.entity.Complaint;
import com.civictransparency.routing.entity.RoutingDecision;
import com.civictransparency.routing.entity.RoutingRule;
import com.civictransparency.routing.repository.RoutingDecisionRepository;
import com.civictransparency.routing.repository.RoutingRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RoutingEngineService {
    
    private final RoutingRuleRepository routingRuleRepository;
    private final RoutingDecisionRepository routingDecisionRepository;
    private final AssignmentService assignmentService;

    public RoutingEngineService(RoutingRuleRepository routingRuleRepository, 
                                RoutingDecisionRepository routingDecisionRepository,
                                AssignmentService assignmentService) {
        this.routingRuleRepository = routingRuleRepository;
        this.routingDecisionRepository = routingDecisionRepository;
        this.assignmentService = assignmentService;
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
    }
}
