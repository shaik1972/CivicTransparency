CREATE TABLE location.complaint_location (
    location_id UUID PRIMARY KEY,
    complaint_id UUID UNIQUE NOT NULL,
    point GEOGRAPHY(POINT,4326),
    accuracy_meters NUMERIC(10,2),
    location_description TEXT,
    exposure_policy VARCHAR(20) NOT NULL,
    public_geometry GEOMETRY,

    CONSTRAINT fk_complaint_location_complaint FOREIGN KEY (complaint_id) REFERENCES complaint.complaint (complaint_id)
);

CREATE INDEX idx_complaint_location_point ON location.complaint_location USING GIST (point);
CREATE INDEX idx_complaint_location_public_geom ON location.complaint_location USING GIST (public_geometry);
