# Civic Transparency — Project Context & Continuity

> **Purpose:** Handoff/continuity document for continuing the Civic Transparency project across ChatGPT sessions.
>
> **Authority:** This is a continuity document, not the architectural source of truth. The frozen documents under `docs/architecture/` and `docs/design/` remain authoritative.
>
> **Last updated:** 2026-08-24

## 1. Project Overview

Civic Transparency is an India-focused civic transparency/accountability platform.

Citizens can report public issues such as potholes, damaged roads, encroachments, poor school infrastructure, unhygienic food outlets, irresponsible driving, and similar civic problems.

Core goals:
- Complaint submission with photos and short videos.
- Strong citizen privacy and PII protection.
- Authority workflow, routing, assignment, SLA and closure.
- Public transparency dashboards.
- Protection/generalization of exact operational locations.
- Moderation, evidence, notification and audit capabilities.
- Security, accessibility, availability and scalability.
- Cloud-portable architecture.

Initial scope:
- One city per state.
- Multilingual support planned.
- Indian identity verification deferred to a later phase.
- Independent citizen/community initiative.

## 2. Authoritative Documents

### Architecture
- `docs/architecture/Civic_Transparency_Architecture & Design Decision Document _V1.0.docx`
- `docs/architecture/Civic_Transparency_Architecture_Design_Freeze_Checklist_V1.0.docx`

### Detailed Design / Database
- `docs/design/Civic_Transparency_Detailed Design Document_Part1_V1.0.docx`
- `docs/design/Civic_Transparency_Detailed Design Document Part1A_V1.0.docx`
- `docs/design/Civic_Transparency_Database & ER DesignPart2_V1.0.docx`
- `docs/design/Civic_Transparency_Detailed_Design_Part_2A_Database_Design_V1.0.docx`
- `docs/design/Civic_Transparency_Detailed_Design_Part_2B_ER_Model_Data_Dictionary_V1.0.docx`

Architecture/design status: **COMPLETE / FROZEN**.

Part 2A + Part 2B are the database implementation authority.

## 3. Technology Stack

- Java 17
- Spring Boot
- Gradle 9.5.1 currently observed
- PostgreSQL 18
- PostGIS 3.6.2
- Flyway
- Spring Data/JPA
- HikariCP
- Tomcat

Development/testing is initially laptop-first because cloud cost is a concern. Architecture must remain cloud-portable.

Java is preferred. A modular monolith is currently appropriate; do not introduce microservices without a genuine independent service boundary.

## 4. Local Database

```text
Database: civic_transparency
Username: civic_app
Host: localhost
Port: 5432
JDBC URL: jdbc:postgresql://localhost:5432/civic_transparency
PostgreSQL service: postgresql-x64-18
PostGIS: 3.6.2
```

Database credentials are supplied through environment variables:

```text
CIVIC_DB_URL
CIVIC_DB_USERNAME
CIVIC_DB_PASSWORD
```

Never commit the actual password.

## 5. Current Database State

V1 created these 17 application schemas:

```text
administration
analytics
assignment
audit
authority
citizen
complaint
evidence
geography
identity
location
moderation
notification
routing
sla
transparency
workflow
```

PostgreSQL `public` schema also exists.

Current Flyway history:

```text
version | description                | success
1       | create application schemas | true
```

## 6. Frozen Database Model

33 tables across 17 application schemas:

### identity
- `user_account`
- `role`
- `user_role`

### citizen
- `citizen_profile`
- `citizen_contact`
- `citizen_address`

### geography
- `state`
- `city`
- `ward`
- `locality`

### complaint
- `complaint`
- `complaint_category`
- `complaint_subcategory`

### location
- `complaint_location`

### workflow
- `workflow`
- `workflow_transition`

### evidence
- `evidence`
- `media_moderation`

### moderation
- `moderation_case`
- `moderation_action`

### authority
- `authority`
- `authority_department`
- `authority_user`

### assignment
- `complaint_assignment`

### routing
- `routing_rule`
- `routing_decision`

### sla
- `sla_policy`
- `complaint_sla`

### notification
- `notification`
- `notification_delivery`

### audit
- `audit_event`

### transparency
- `public_complaint`

### analytics
- `complaint_metrics`

### administration
- `system_configuration`

## 7. Key Database Decisions

### Privacy
Citizen PII is isolated in the `citizen` schema. Complaint records should not duplicate citizen name, email, phone, address, password, OTP or authentication tokens.

### Workflow
Workflow owns the authoritative complaint lifecycle state. Do not create a competing complaint status field contrary to the frozen model.

### Location
Operational exact coordinates are protected. Public representation uses generalized geometry and exposure policy.

Exposure concepts:
- EXACT
- AREA
- WARD
- CITY
- HIDDEN

PostGIS is used for spatial data; important spatial columns require GIST indexes.

### Routing
Routing is deterministic: matching specificity first, priority as tie-breaker. Routing decisions are historical records.

### SLA
Initial model uses elapsed time. `hold_pauses_sla` controls whether ON_HOLD pauses SLA timing. Business-calendar support is deferred.

### Public transparency
Public dashboards use a transparency projection/read model rather than directly exposing operational tables. Eventual consistency is acceptable.

### Audit
`audit.audit_event` is append-only and protected against unauthorized mutation.

### Media
PostgreSQL stores media metadata/storage references/moderation/validation information. Actual image/video bytes live behind an object-storage abstraction.

### IDs and time
- UUIDs for major domain identifiers.
- `TIMESTAMPTZ` for timestamps.
- UTC-oriented application handling.
- JSONB only where data is genuinely variable.
- Soft delete only where domain-required.
- Avoid destructive cascading deletes for historical/reference data.

## 8. V1

Migration:

`backend/src/main/resources/db/migration/V1__create_application_schemas.sql`

Purpose: create the application schemas.

Status: **COMPLETE**.

Do not modify an already-applied Flyway migration to change the schema. Create a new migration.

## 9. Migration Roadmap

```text
V1   Application schemas                  COMPLETE
V2   Geography foundation                  NEXT
V3   Identity + Citizen
V4   Complaint + categories
V5   Location + Evidence
V6   Workflow
V7   Authority + Assignment
V8   Routing
V9   SLA
V10  Notification
V11  Audit
V12  Transparency projection
V13  Analytics
V14  Administration
```

The exact boundaries may change only when a genuine dependency requires it. Avoid artificial micro-migrations.

## 10. Immediate Next Task — V2 Geography

File:

`backend/src/main/resources/db/migration/V2__create_geography_tables.sql`

V2 should create structure only. Do not seed Karnataka/Bengaluru or other reference data yet.

Hierarchy:

```text
state
  ↓
city
  ↓
ward
  ↓
locality
```

Core frozen attributes:

### geography.state
- `state_id`
- `code`
- `name`
- `is_active`

### geography.city
- `city_id`
- `state_id`
- `code`
- `name`
- `is_active`

### geography.ward
- `ward_id`
- `city_id`
- `code`
- `name`
- `is_active`

### geography.locality
- `locality_id`
- `ward_id`
- `name`
- `is_active`

Agreed implementation decisions:
- `state.code` globally unique.
- `city.code` unique within its state.
- `ward.code` unique within its city.
- Names are contextual, not globally unique.
- Restrictive FK deletion; no accidental cascading deletion.
- UUID primary keys.
- Use `TIMESTAMPTZ` for standard technical timestamps if included.
- Do not add unapproved business attributes.
- No geography seed data in V2.

## 11. Migration Workflow

For every migration:

1. Design SQL.
2. Compare it with the frozen Part 2A/2B design.
3. Review SQL before execution.
4. Create Flyway migration.
5. Run Flyway through Gradle/Spring Boot.
6. Verify PostgreSQL objects directly.
7. Run backend tests.
8. Check Git status.
9. Commit the milestone.

Flyway owns schema evolution. Hibernate/JPA must not compete with Flyway for schema creation/update.

## 12. Current Gradle Memory Configuration

`backend/gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx512m -XX:MaxMetaspaceSize=256m
org.gradle.daemon=true
```

The laptop has approximately 5.85 GB visible RAM. Earlier JVM/Gradle failures were caused by severe memory pressure.

Do not increase JVM/Gradle memory aggressively without checking available RAM.

## 13. Runtime / Database Issues Already Resolved

### JVM memory
An earlier `bootRun` failed because available RAM was extremely low. Later the Spring Boot application successfully started on port 8080.

Health check succeeded:

```text
GET http://localhost:8080/actuator/health
status = UP
```

A later `clean build` completed successfully.

### PostGIS
PostGIS 3.6.2 was installed for PostgreSQL 18 using StackBuilder and enabled successfully.

## 14. Git Baseline

Current branch:

```text
master
```

Initial commit:

```text
49a34df
Initial project baseline: architecture, design and backend foundation
```

The working tree was clean after the baseline commit.

The baseline includes the backend foundation, Gradle configuration, Flyway V1, tests, architecture documents, detailed design/database documents and Git ignore configuration.

## 15. Git / Secret Rules

Never commit:
- passwords
- `.env` files
- real database credentials
- `build/`
- `.gradle/`
- `bin/`
- IDE metadata
- JVM crash logs
- temporary files
- local tool/session data

## 16. Development Philosophy

- Architecture first, implementation second.
- Frozen design is the baseline.
- Explicitly document deviations.
- Prefer simple modular boundaries over premature microservices.
- Keep the architecture cloud-portable.
- Develop/test locally first.
- Treat security and privacy as architectural concerns.
- Version database changes with Flyway.
- Preserve audit/history.
- Do not expose PII through public or operational APIs.
- Avoid over-engineering.

## 17. Resume Instructions for a New ChatGPT Session

Start a new session with:

> I am continuing the Civic Transparency project. Please read `docs/project-context/PROJECT_CONTEXT.md` first, then read the relevant authoritative documents under `docs/architecture/` and `docs/design/`. Do not redesign frozen architecture decisions unless I explicitly ask. Check the current Git, database and application state before making implementation changes.

For database work, prioritize:
1. Architecture Design Freeze Checklist.
2. Detailed Design Part 2A.
3. Detailed Design Part 2B / ER Model & Data Dictionary.
4. This context document.

## 18. Current Position

```text
Architecture                         COMPLETE / FROZEN
Detailed design                      COMPLETE / FROZEN
Database ER/data dictionary          COMPLETE / FROZEN
Repository baseline                  COMPLETE
PostgreSQL 18                        READY
PostGIS 3.6.2                       READY
Flyway V1                            COMPLETE
Spring Boot foundation               READY
Git baseline                         COMPLETE

NEXT:
V2__create_geography_tables.sql
```

## 19. Critical Decisions Not to Lose

1. Citizen PII is isolated from complaint/public data.
2. Citizen identity must be protected from authorities where applicable.
3. Exact operational location is not public by default.
4. Public geometry is generalized through exposure policy.
5. Workflow owns authoritative complaint state.
6. Routing is deterministic and historical decisions are retained.
7. SLA initially uses elapsed-time semantics.
8. Public transparency uses a projection/read model.
9. Audit events are append-only.
10. Media bytes live outside PostgreSQL.
11. Flyway owns database schema evolution.
12. Hibernate/JPA must not compete with Flyway.
13. Architecture remains cloud-portable despite laptop-first development.
14. Avoid premature microservices.
15. Do not change frozen architecture/design casually.

---

**End of Project Context**
