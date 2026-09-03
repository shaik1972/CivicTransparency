# Design Plan — Category and Location Selection

**Document Version:** 1.0  
**Target Domain:** Complaint Categories & Geography Selection Foundation  
**Status:** PROPOSED  

---

## 1. Overview

Before citizens can submit civic complaints, the system requires structured selection of:
1. **Issue Category & Subcategory**: Standardized classification of civic problems (e.g., Roads → Pothole, Safety → Streetlight).
2. **Geography & Location Context**: State → City → Ward → Locality selection with landmark/address details.

This plan details the database migrations (`V6`, `V7`), backend JPA entities, Spring Data repositories, REST endpoints, automated test suites, and Next.js frontend selection components.

---

## 2. Database Design & Flyway Migrations

### Migration `V6__create_and_seed_complaint_categories.sql`

#### `complaint.complaint_category`
* `category_id` (UUID, PRIMARY KEY)
* `code` (VARCHAR(50), NOT NULL, UNIQUE)
* `name` (VARCHAR(100), NOT NULL)
* `description` (TEXT)
* `icon_name` (VARCHAR(50))
* `display_order` (INT, DEFAULT 0)
* `is_active` (BOOLEAN, NOT NULL, DEFAULT TRUE)
* `created_at` (TIMESTAMPTZ, NOT NULL)
* `updated_at` (TIMESTAMPTZ, NOT NULL)

#### `complaint.complaint_subcategory`
* `subcategory_id` (UUID, PRIMARY KEY)
* `category_id` (UUID, NOT NULL, FK → `complaint.complaint_category.category_id`)
* `code` (VARCHAR(50), NOT NULL, UNIQUE)
* `name` (VARCHAR(100), NOT NULL)
* `description` (TEXT)
* `default_priority` (VARCHAR(20), NOT NULL DEFAULT 'MEDIUM')
* `is_active` (BOOLEAN, NOT NULL, DEFAULT TRUE)
* `created_at` (TIMESTAMPTZ, NOT NULL)
* `updated_at` (TIMESTAMPTZ, NOT NULL)

#### Initial Seeded Categories & Subcategories
1. **ROADS** (Roads & Infrastructure)
   * `ROADS_POTHOLE` (Pothole / Damaged Road Surface)
   * `ROADS_FOOTPATH` (Footpath Encroachment / Damaged Pavement)
   * `ROADS_DIVIDER` (Damaged Road Divider / Missing Signage)
2. **SANITATION** (Sanitation & Waste Management)
   * `SANITATION_GARBAGE` (Overflowing Garbage Bin / Street Dumping)
   * `SANITATION_ANIMAL` (Dead Animal Removal Required)
   * `SANITATION_DRAIN` (Open / Clogged Surface Drain)
3. **WATER** (Water Supply & Sewerage)
   * `WATER_LEAKAGE` (Pipeline Leakage / Water Contamination)
   * `WATER_MANHOLE` (Open / Damaged Sewage Manhole)
   * `WATER_SHORTAGE` (Public Water Supply Disruption)
4. **SAFETY** (Public Safety & Street Lighting)
   * `SAFETY_STREETLIGHT` (Streetlight Not Functioning)
   * `SAFETY_WIRING` (Damaged Electric Pole / Exposed Wiring)
   * `SAFETY_HAZARD` (Hazardous Tree Branch / Structural Threat)
5. **SCHOOL** (Educational & School Infrastructure)
   * `SCHOOL_STRUCTURE` (Damaged School Roof / Building Defect)
   * `SCHOOL_SANITATION` (Unsanitary / Non-functional School Toilets)
6. **FOOD** (Food Hygiene & Public Health)
   * `FOOD_HYGIENE` (Unhygienic Food Outlet / Unsafe Food Vendor)
   * `FOOD_WATER` (Contaminated Water Supply at Food Stall)

---

### Migration `V7__seed_bengaluru_wards_and_localities.sql`

#### `geography.ward` Table Structure (Frozen V2 Schema)
* `ward_id` (UUID, PRIMARY KEY)
* `city_id` (UUID, NOT NULL, FK → `geography.city.city_id`)
* `code` (VARCHAR(50), NOT NULL, UNIQUE within city)
* `name` (VARCHAR(100), NOT NULL)
* `is_active` (BOOLEAN, NOT NULL, DEFAULT TRUE)

#### `geography.locality` Table Structure (Frozen V2 Schema)
* `locality_id` (UUID, PRIMARY KEY)
* `ward_id` (UUID, NOT NULL, FK → `geography.ward.ward_id`)
* `name` (VARCHAR(100), NOT NULL)
* `is_active` (BOOLEAN, NOT NULL, DEFAULT TRUE)

#### Initial Seeded Bengaluru Wards & Localities
* **Ward 1 (Koramangala)**: Koramangala 1st Block, 4th Block, 5th Block, 80ft Road.
* **Ward 2 (Indiranagar)**: 100ft Road, 12th Main, Defense Colony.
* **Ward 3 (HSR Layout)**: HSR Sector 1, HSR Sector 2, HSR Sector 3.
* **Ward 4 (Whitefield)**: EPIP Zone, ITPL Road, Siddapura.
* **Ward 5 (Jayanagar)**: 3rd Block, 4th Block, 9th Block.
* **Ward 6 (Malleshwaram)**: Margosa Road, Sampige Road, 18th Cross.

---

## 3. Backend Architecture

### REST API Endpoints
1. `GET /api/v1/categories`: Returns hierarchical list of active categories and subcategories.
2. `GET /api/v1/geography/cities/{cityId}/wards`: Returns active wards for a given city.
3. `GET /api/v1/geography/wards/{wardId}/localities`: Returns active localities for a given ward.

---

## 4. Frontend Component

* **CategoryLocationSelector.tsx**: An interactive React component providing:
  * Category grid with visual icons and subcategory dropdown.
  * City → Ward → Locality selection hierarchy.
  * Street address / landmark entry field.
