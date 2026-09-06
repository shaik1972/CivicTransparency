CREATE TABLE assignment.complaint_assignment (
    assignment_id UUID PRIMARY KEY,
    complaint_id UUID REFERENCES complaint.complaint(complaint_id),
    department_id UUID REFERENCES authority.authority_department(department_id),
    assigned_at TIMESTAMPTZ NOT NULL,
    unassigned_at TIMESTAMPTZ,
    assignment_reason VARCHAR(50),
    assigned_by UUID REFERENCES identity.user_account(user_id)
);

CREATE INDEX idx_complaint_assignment_comp_unassigned ON assignment.complaint_assignment(complaint_id, unassigned_at);
CREATE INDEX idx_complaint_assignment_dept_assigned ON assignment.complaint_assignment(department_id, assigned_at);
