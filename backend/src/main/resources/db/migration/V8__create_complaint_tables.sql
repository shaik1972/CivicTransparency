CREATE TABLE complaint.complaint (
    complaint_id UUID PRIMARY KEY,
    complaint_reference VARCHAR(30) UNIQUE NOT NULL,
    citizen_id UUID NOT NULL,
    category_id UUID NOT NULL,
    subcategory_id UUID,
    title VARCHAR(250) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(30) NOT NULL,
    city_id UUID NOT NULL,
    ward_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL,

    CONSTRAINT fk_complaint_citizen FOREIGN KEY (citizen_id) REFERENCES citizen.citizen_profile (citizen_id),
    CONSTRAINT fk_complaint_category FOREIGN KEY (category_id) REFERENCES complaint.complaint_category (category_id),
    CONSTRAINT fk_complaint_subcategory FOREIGN KEY (subcategory_id) REFERENCES complaint.complaint_subcategory (subcategory_id),
    CONSTRAINT fk_complaint_city FOREIGN KEY (city_id) REFERENCES geography.city (city_id),
    CONSTRAINT fk_complaint_ward FOREIGN KEY (ward_id) REFERENCES geography.ward (ward_id)
);

CREATE INDEX idx_complaint_city_created ON complaint.complaint (city_id, created_at);
CREATE INDEX idx_complaint_category_created ON complaint.complaint (category_id, created_at);
