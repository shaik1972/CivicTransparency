package com.civictransparency.workflow.repository;

import com.civictransparency.workflow.entity.WorkflowTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransition, UUID> {
    List<WorkflowTransition> findByWorkflowWorkflowIdOrderByCreatedAtAsc(UUID workflowId);
}
