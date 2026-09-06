CREATE TABLE routing.routing_rule (
    rule_id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    state_id UUID REFERENCES geography.state(state_id),
    city_id UUID REFERENCES geography.city(city_id),
    ward_id UUID REFERENCES geography.ward(ward_id),
    category_id UUID REFERENCES complaint.complaint_category(category_id),
    subcategory_id UUID REFERENCES complaint.complaint_subcategory(subcategory_id),
    severity VARCHAR(30),
    department_id UUID REFERENCES authority.authority_department(department_id),
    priority INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL,
    effective_from TIMESTAMPTZ,
    effective_to TIMESTAMPTZ
);

CREATE INDEX idx_routing_rule_active_priority ON routing.routing_rule(is_active, priority);

CREATE TABLE routing.routing_decision (
    decision_id UUID PRIMARY KEY,
    complaint_id UUID REFERENCES complaint.complaint(complaint_id),
    rule_id UUID REFERENCES routing.routing_rule(rule_id),
    department_id UUID REFERENCES authority.authority_department(department_id),
    specificity_score INTEGER NOT NULL,
    decided_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_routing_decision_comp_decided ON routing.routing_decision(complaint_id, decided_at);
