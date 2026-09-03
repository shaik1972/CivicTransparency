-- ============================================================
-- Civic Transparency
-- Flyway Migration V6
-- Complaint Category & Subcategory Reference Data
--
-- Creates and seeds:
--   complaint.complaint_category
--   complaint.complaint_subcategory
--
-- Standard categories for the initial Bengaluru rollout.
-- ============================================================

CREATE TABLE complaint.complaint_category (
    category_id   UUID         PRIMARY KEY,
    code          VARCHAR(50)  NOT NULL,
    name          VARCHAR(100) NOT NULL,
    description   TEXT,
    icon_name     VARCHAR(50),
    display_order INT          NOT NULL DEFAULT 0,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT uk_complaint_category_code UNIQUE (code)
);

CREATE TABLE complaint.complaint_subcategory (
    subcategory_id   UUID         PRIMARY KEY,
    category_id      UUID         NOT NULL,
    code             VARCHAR(50)  NOT NULL,
    name             VARCHAR(100) NOT NULL,
    description      TEXT,
    default_priority VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_subcategory_category
        FOREIGN KEY (category_id)
        REFERENCES complaint.complaint_category (category_id),

    CONSTRAINT uk_complaint_subcategory_code UNIQUE (code)
);

-- ──────────────────────────────────────────────────────────
-- Seed Categories
-- ──────────────────────────────────────────────────────────

INSERT INTO complaint.complaint_category (category_id, code, name, description, icon_name, display_order) VALUES
    ('a1000000-0000-4000-8000-000000000001', 'ROADS',      'Roads & Infrastructure',      'Potholes, damaged roads, pavements and dividers',           'road',          1),
    ('a1000000-0000-4000-8000-000000000002', 'SANITATION',  'Sanitation & Waste',          'Overflowing bins, street dumping, open drains',             'trash',         2),
    ('a1000000-0000-4000-8000-000000000003', 'WATER',       'Water Supply & Sewerage',     'Pipeline leakage, manhole issues, water shortage',          'droplet',       3),
    ('a1000000-0000-4000-8000-000000000004', 'SAFETY',      'Public Safety & Lighting',    'Streetlights, electrical hazards, structural threats',      'shield',        4),
    ('a1000000-0000-4000-8000-000000000005', 'SCHOOL',      'School Infrastructure',       'School building defects, sanitation issues',                'school',        5),
    ('a1000000-0000-4000-8000-000000000006', 'FOOD',        'Food Hygiene & Public Health','Unhygienic food outlets, contaminated water at vendors',    'utensils',      6);

-- ──────────────────────────────────────────────────────────
-- Seed Subcategories
-- ──────────────────────────────────────────────────────────

-- ROADS
INSERT INTO complaint.complaint_subcategory (subcategory_id, category_id, code, name, description, default_priority) VALUES
    ('b1000000-0000-4000-8000-000000000001', 'a1000000-0000-4000-8000-000000000001', 'ROADS_POTHOLE',   'Pothole / Damaged Road Surface', 'Deep potholes or broken road surface posing risk',          'HIGH'),
    ('b1000000-0000-4000-8000-000000000002', 'a1000000-0000-4000-8000-000000000001', 'ROADS_FOOTPATH',  'Footpath / Pavement Damage',     'Broken footpath, encroachment or missing pavement',         'MEDIUM'),
    ('b1000000-0000-4000-8000-000000000003', 'a1000000-0000-4000-8000-000000000001', 'ROADS_DIVIDER',   'Road Divider / Signage Issue',   'Damaged divider, missing road signs or reflectors',         'MEDIUM');

-- SANITATION
INSERT INTO complaint.complaint_subcategory (subcategory_id, category_id, code, name, description, default_priority) VALUES
    ('b1000000-0000-4000-8000-000000000004', 'a1000000-0000-4000-8000-000000000002', 'SANITATION_GARBAGE', 'Overflowing Garbage / Street Dumping', 'Overflowing bins or uncontrolled garbage accumulation',   'HIGH'),
    ('b1000000-0000-4000-8000-000000000005', 'a1000000-0000-4000-8000-000000000002', 'SANITATION_ANIMAL',  'Dead Animal Removal',                  'Dead animal carcass requiring immediate removal',         'HIGH'),
    ('b1000000-0000-4000-8000-000000000006', 'a1000000-0000-4000-8000-000000000002', 'SANITATION_DRAIN',   'Open / Clogged Surface Drain',         'Open or clogged drain causing waterlogging or odour',     'MEDIUM');

-- WATER
INSERT INTO complaint.complaint_subcategory (subcategory_id, category_id, code, name, description, default_priority) VALUES
    ('b1000000-0000-4000-8000-000000000007', 'a1000000-0000-4000-8000-000000000003', 'WATER_LEAKAGE',   'Pipeline Leakage / Contamination', 'Burst pipe, visible leakage or contaminated water supply', 'HIGH'),
    ('b1000000-0000-4000-8000-000000000008', 'a1000000-0000-4000-8000-000000000003', 'WATER_MANHOLE',   'Open / Damaged Sewage Manhole',    'Open or collapsed manhole posing safety risk',             'HIGH'),
    ('b1000000-0000-4000-8000-000000000009', 'a1000000-0000-4000-8000-000000000003', 'WATER_SHORTAGE',  'Public Water Supply Disruption',   'Water supply cut-off or inadequate supply',                'MEDIUM');

-- SAFETY
INSERT INTO complaint.complaint_subcategory (subcategory_id, category_id, code, name, description, default_priority) VALUES
    ('b1000000-0000-4000-8000-000000000010', 'a1000000-0000-4000-8000-000000000004', 'SAFETY_STREETLIGHT', 'Streetlight Not Functioning',       'One or more streetlights are off or damaged',              'MEDIUM'),
    ('b1000000-0000-4000-8000-000000000011', 'a1000000-0000-4000-8000-000000000004', 'SAFETY_WIRING',      'Damaged Electric Pole / Wiring',    'Dangling wires or damaged pole posing electrocution risk', 'HIGH'),
    ('b1000000-0000-4000-8000-000000000012', 'a1000000-0000-4000-8000-000000000004', 'SAFETY_HAZARD',      'Hazardous Tree / Structural Threat', 'Fallen or dangerous tree branch, collapsing structure',    'HIGH');

-- SCHOOL
INSERT INTO complaint.complaint_subcategory (subcategory_id, category_id, code, name, description, default_priority) VALUES
    ('b1000000-0000-4000-8000-000000000013', 'a1000000-0000-4000-8000-000000000005', 'SCHOOL_STRUCTURE',   'Damaged School Building / Roof',      'Roof leak, wall crack or structural defect in school',    'HIGH'),
    ('b1000000-0000-4000-8000-000000000014', 'a1000000-0000-4000-8000-000000000005', 'SCHOOL_SANITATION',  'Unsanitary School Toilet Facilities', 'Broken, blocked or unusable school toilet blocks',        'HIGH');

-- FOOD
INSERT INTO complaint.complaint_subcategory (subcategory_id, category_id, code, name, description, default_priority) VALUES
    ('b1000000-0000-4000-8000-000000000015', 'a1000000-0000-4000-8000-000000000006', 'FOOD_HYGIENE', 'Unhygienic Food Outlet / Vendor',  'Unsanitary conditions at food stall or restaurant',        'HIGH'),
    ('b1000000-0000-4000-8000-000000000016', 'a1000000-0000-4000-8000-000000000006', 'FOOD_WATER',   'Contaminated Water at Food Outlet', 'Unsafe or contaminated water being used at food vendor',   'HIGH');
