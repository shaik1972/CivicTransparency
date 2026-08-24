-- ============================================================
-- Civic Transparency
-- Flyway Migration V2
-- Geography Foundation
--
-- Hierarchy:
--   state -> city -> ward -> locality
--
-- Design authority:
--   Detailed Design Part 2A / Part 2B
-- ============================================================

CREATE TABLE geography.state (
    state_id UUID PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_active BOOLEAN NOT NULL,

    CONSTRAINT uk_state_code UNIQUE (code),
    CONSTRAINT uk_state_name UNIQUE (name)
);

CREATE TABLE geography.city (
    city_id UUID PRIMARY KEY,
    state_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    is_active BOOLEAN NOT NULL,

    CONSTRAINT fk_city_state
        FOREIGN KEY (state_id)
        REFERENCES geography.state (state_id),

    CONSTRAINT uk_city_state_code
        UNIQUE (state_id, code)
);

CREATE TABLE geography.ward (
    ward_id UUID PRIMARY KEY,
    city_id UUID NOT NULL,
    code VARCHAR(30) NOT NULL,
    name VARCHAR(150) NOT NULL,
    is_active BOOLEAN NOT NULL,

    CONSTRAINT fk_ward_city
        FOREIGN KEY (city_id)
        REFERENCES geography.city (city_id),

    CONSTRAINT uk_ward_city_code
        UNIQUE (city_id, code)
);

CREATE TABLE geography.locality (
    locality_id UUID PRIMARY KEY,
    ward_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    is_active BOOLEAN NOT NULL,

    CONSTRAINT fk_locality_ward
        FOREIGN KEY (ward_id)
        REFERENCES geography.ward (ward_id)
);
