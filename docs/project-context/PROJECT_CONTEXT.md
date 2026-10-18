# Civic Transparency — Project Context & Continuity

> **Purpose:** Handoff/continuity document for continuing the Civic Transparency project across ChatGPT sessions.
>
> **Authority:** This is a continuity document, not the architectural source of truth. The frozen documents under `docs/architecture/` and `docs/design/` remain authoritative.
>
> **Last updated:** 2026-10-18

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

## 9. Migration Progress

V18 adds authority roles and SLA hold behavior. The full backend test suite passed on 2026-10-18, including Flyway startup and Hibernate schema validation. The historical roadmap in earlier versions of this document used a different numbering scheme; the migration files below are the current source of truth.

```text
V1   Application schemas                  COMPLETE
V2   Geography foundation                  COMPLETE
V3   Identity + citizen                    COMPLETE
V4   Identity reference roles               COMPLETE
V5   Geography reference data               COMPLETE
V6   Complaint categories                   COMPLETE
V7   Bengaluru wards + localities           COMPLETE
V8   Complaint tables                       COMPLETE
V9   Location tables                        COMPLETE
V10  Workflow tables                       COMPLETE
V11  Evidence tables                        COMPLETE
V12  Authority tables                       COMPLETE
V13  Assignment tables                      COMPLETE
V14  Routing tables                         COMPLETE
V15  SLA tables                             COMPLETE
V16  Notification tables                    COMPLETE
V17  Audit tables                           COMPLETE
V18  Authority roles + SLA hold behavior    IMPLEMENTED / VALIDATED BY BACKEND TESTS
```

V14 is **routing** in the implemented migration history, not administration. Transparency, analytics, and moderation remain future slices. The `administration`, `transparency`, `analytics`, and `moderation` schemas were created by V1, but those domain migrations and workflows have not yet been implemented. The authority/routing/SLA configuration APIs described below live in the `admin` application package and use the existing authority, routing, and SLA schemas.

## 10. Current Implementation Status

Completed backend foundation:
- Citizen registration and authentication.
- Geography and category lookup APIs.
- Complaint submission with location, evidence, workflow initialization, and citizen complaint listing.
- Authority, assignment, and routing domain entities, repositories, and services.
- SLA, notification, and audit domain entities, repositories, and services.
- Initial workflow transition recording when a complaint is submitted.
- Authority staff authentication, with roles read from signed access-token claims.
- `SYSTEM_ADMIN`-protected configuration APIs to create/list authorities, departments, authority users, routing rules, and SLA policies; status/activation updates are available for authorities, departments, rules, and policies.
- System administration UI at `/admin` provisions authorities, departments, authority user accounts, and routing rules; the initial `SYSTEM_ADMIN` still requires secure out-of-band bootstrap.
- Dedicated authority sign-in at `/authority/login` uses the existing role-aware login endpoint; authority accounts are provisioned by `SYSTEM_ADMIN`, never public self-registration.
- `AUTHORITY_ADMIN` and `AUTHORITY_OFFICER` complaint operations, scoped to the staff member's active department; `SYSTEM_ADMIN` can configure and cross-department reassign.
- Authority portal queue lists only currently assigned complaints for the signed-in staff member's active department; staff can update lifecycle status with a required reason.
- Citizens can view current status and transition history only for their own complaints.
- Optional department assignment emails route by department code and contain only the complaint reference and portal link; disabled by default.
- Workflow transitions with a validated lifecycle, audit events, actor attribution, and transition history.
- Assignment/reassignment history, with the prior active assignment closed atomically when a new assignment is created.
- SLA pause/resume/resolve behavior; ON_HOLD pauses only when the matched policy enables it, and resume extends the deadline by the paused duration.

Completed routing/SLA behavior:
- Routing filters rules by complaint severity.
- Routing selects the most specific matching rule, then priority, then a stable rule identifier; the destination authority's state/city scope is also enforced.
- Unresolved routing creates an audit event and still attempts SLA initialization.
- SLA initialization is idempotent and policy matching returns the most specific active policy, preferring subcategory-specific over category-wide policies.
- Routing rules and SLA policies have application-level constructors for configuration and tests.

Previously completed validation on 2026-09-18 (before the workflow changes below):
- Backend `gradlew build` passed.
- Frontend `npm run lint` passed.
- Frontend `npm run build` passed.
- Complaint and notification integration tests passed.

Current workflow and authority portal verification:
- Added `WorkflowOperationsIntegrationTest` covering management authorization, staff login, specificity-based routing, assignment history/reassignment, workflow authorization/transitions, and SLA pause/resume/resolve.
- Added department-isolated queue and citizen-owned complaint status/history coverage.
- Added department email notifier tests for recipient selection and minimal email content.
- The full backend test suite, including V18 migration/schema validation, passed on 2026-10-18.
- Frontend lint and production build passed on 2026-10-18 with the authority login, system-admin setup, authority portal, and citizen status timeline.
- Full browser regression and actual SMTP capture testing remain pending.
- Initial `SYSTEM_ADMIN` assignment is intentionally an out-of-band secure bootstrap operation; no public endpoint can grant system-administrator privileges.

## 11. Pending Work Slices

1. Provision the first `SYSTEM_ADMIN` account/role through the approved secure bootstrap process and configure authority users, departments, and routing rules.
2. Complete browser regression using citizen and authority accounts; complete local SMTP capture testing.
3. Complete notification operations for inbox listing, read state, delivery retries, and delivery status updates.
4. Add audit query APIs and enforce append-only behavior at the application and database boundaries.
5. Implement moderation tables and workflows for evidence/media review.
6. Implement the transparency projection and public complaint/dashboard APIs.
7. Implement analytics projections and complaint metrics.
8. Complete broader production security/token-lifecycle verification.
9. Update deployment, observability, privacy, and operational documentation for production use.

The exact boundaries may change only when a genuine dependency requires it. Avoid artificial micro-migrations.

## 12. Geography Reference Model

The V2-V7 geography work established this hierarchy:

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

## 13. Migration Workflow

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

## 14. Current Gradle Memory Configuration

`backend/gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx512m -XX:MaxMetaspaceSize=256m
org.gradle.daemon=true
```

The laptop has approximately 5.85 GB visible RAM. Earlier JVM/Gradle failures were caused by severe memory pressure.

Do not increase JVM/Gradle memory aggressively without checking available RAM.

## 15. Runtime / Database Issues Already Resolved

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

## 16. Git Baseline

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

## 17. Git / Secret Rules

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

## 18. Development Philosophy

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

## 19. Resume Instructions for a New ChatGPT Session

Start a new session with:

> I am continuing the Civic Transparency project. Please read `docs/project-context/PROJECT_CONTEXT.md` first, then read the relevant authoritative documents under `docs/architecture/` and `docs/design/`. Do not redesign frozen architecture decisions unless I explicitly ask. Check the current Git, database and application state before making implementation changes.

For database work, prioritize:
1. Architecture Design Freeze Checklist.
2. Detailed Design Part 2A.
3. Detailed Design Part 2B / ER Model & Data Dictionary.
4. This context document.

## 20. Current Position

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

## 21. Critical Decisions Not to Lose

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

V2 Geography Foundation completed
Migration:
backend/src/main/resources/db/migration/V2__create_geography_tables.sql
Tables:- geography.state
- geography.city
- geography.ward
- geography.locality

FK hierarchy:
state → city → ward → locality
Unique constraints verified
Flyway:- V1 ✅
- V2 ✅

PostgreSQL 18.6 verified
PostGIS 3.6.2 installed/enabled
Spring Boot startup verified
Git commit:
b22ef9e Add geography foundation schema
Working tree clean
Current project checkpoint: V2 complete; V3 not started

V3 Identity + Citizen completed

Migration:

`backend/src/main/resources/db/migration/V3__create_identity_and_citizen_tables.sql`

Tables:
- `identity.user_account`
- `identity.role`
- `identity.user_role`
- `citizen.citizen_profile`
- `citizen.citizen_contact`
- `citizen.citizen_address`

The V3 model follows Part 2B exactly: profile/contact/address PII remains in the `citizen` schema; operational modules receive only controlled references. No role or citizen seed data was added.

Browser-visible application page:

`backend/src/main/resources/static/index.html` is served at `http://localhost:8080/` when Spring Boot is running. It is a first landing page only; reporting and dashboard actions intentionally remain future vertical slices.

Current project checkpoint: V3 implementation complete; local Flyway/PostgreSQL and Gradle verification pending. The current task session did not receive `CIVIC_DB_*` credentials, and Gradle verification must be run when sufficient local memory is available. Not committed.


# V3 Verification Status and Delta — 2026-08-27

## 1. V3 Verification Baseline

V3 is the database foundation for the Identity and Citizen domains.

The V3 migration file already exists locally:

`V3__create_identity_and_citizen_tables.sql`

Location:

`backend/src/main/resources/db/migration/V3__create_identity_and_citizen_tables.sql`

The V3 SQL was reviewed against the frozen Detailed Design Part 2A / Part 2B database design.

No SQL changes were made during the V3 verification performed on 2026-08-27.

---

## 2. Previous State Before V3 Verification

Before V3 was applied:

* Flyway history contained only V1 and V2.
* V1 created the application schemas.
* V2 created the geography tables.
* Identity and Citizen tables had not yet been physically created in PostgreSQL.
* No Java backend classes for V3 had been implemented.
* No React/frontend classes for V3 had been implemented.
* No V3 application APIs had been implemented.
* No V3 browser/UI functionality had been implemented.

The project therefore had a database-design/migration foundation but not yet the V3 application implementation layer.

---

## 3. Local Database Configuration

Local PostgreSQL configuration was verified through PowerShell.

Configured environment variables:

* `CIVIC_DB_USERNAME = civic_app`
* `CIVIC_DB_URL = jdbc:postgresql://localhost:5432/civic_transparency`
* `CIVIC_DB_PASSWORD = configured locally and intentionally not recorded in project documentation`

PostgreSQL version observed during V3 migration execution:

`PostgreSQL 18.6`

---

## 4. Flyway Verification

The following command was attempted:

`.\gradlew flywayInfo`

Result:

`Task 'flywayInfo' not found.`

This was determined not to be a Flyway migration failure.

The current Gradle project does not expose Flyway commands as Gradle tasks.

V3 migration execution was instead verified through Spring Boot startup using:

`.\gradlew bootRun`

Spring Boot successfully connected to the local PostgreSQL database.

Flyway successfully validated all three migrations and applied V3:

`Migrating schema public to version 3 - create identity and citizen tables`

`Successfully applied 1 migration ... now at version v3`

Post-migration Flyway history was verified as:

* V1 — create application schemas — success
* V2 — create geography tables — success
* V3 — create identity and citizen tables — success

---

## 5. V3 Tables Verified

All six V3 tables were physically verified in PostgreSQL.

### Identity schema

* `identity.user_account`
* `identity.role`
* `identity.user_role`

### Citizen schema

* `citizen.citizen_profile`
* `citizen.citizen_contact`
* `citizen.citizen_address`

Result:

**V3 table verification — PASS**

---

## 6. V3 Column Verification

PostgreSQL `information_schema` verification confirmed all 30 expected V3 columns.

Data types, VARCHAR lengths, and nullability were verified against the frozen Part 2B database design.

Result:

**V3 column verification — PASS**

---

## 7. V3 Primary Key and Unique Constraint Verification

The following primary keys were verified:

* `identity.user_account.user_id`
* `identity.role.role_id`
* `identity.user_role (user_id, role_id)` — composite primary key
* `citizen.citizen_profile.citizen_id`
* `citizen.citizen_contact.citizen_id`
* `citizen.citizen_address.address_id`

The following UNIQUE constraints were verified:

* `identity.user_account.email`
* `identity.user_account.username`
* `identity.role.code`
* `citizen.citizen_profile.user_id`

Result:

**V3 PK and UNIQUE constraint verification — PASS**

---

## 8. V3 Foreign Key Verification

The live PostgreSQL database was verified directly through the PostgreSQL `pg_constraint` catalog.

All six expected V3 foreign-key relationships are present:

1. `identity.user_role.user_id`
   → `identity.user_account.user_id`

2. `identity.user_role.role_id`
   → `identity.role.role_id`

3. `citizen.citizen_profile.user_id`
   → `identity.user_account.user_id`

4. `citizen.citizen_contact.citizen_id`
   → `citizen.citizen_profile.citizen_id`

5. `citizen.citizen_address.citizen_id`
   → `citizen.citizen_profile.citizen_id`

6. `citizen.citizen_address.city_id`
   → `geography.city.city_id`

Result:

**V3 foreign-key verification — PASS**

Note: An earlier `information_schema` query returned only four relationships. A direct PostgreSQL `pg_constraint` query subsequently confirmed that all six foreign keys are physically present. No database or migration change was required.

---

## 9. V3 Referential Action Verification

All six V3 foreign keys were verified directly against PostgreSQL.

Current referential actions:

* `ON DELETE = NO ACTION`
* `ON UPDATE = NO ACTION`

for all six V3 foreign-key relationships.

This matches the behavior produced by the V3 migration, which does not specify alternative `ON DELETE` or `ON UPDATE` actions.

Result:

**V3 referential-action verification — PASS**

---

## 10. V3 Index Verification

The live PostgreSQL database contains 10 indexes associated with V3 primary-key and UNIQUE constraints.

Verified indexes include:

### Citizen

* `citizen_address_pkey`
* `citizen_contact_pkey`
* `citizen_profile_pkey`
* `citizen_profile_user_id_key`

### Identity

* `role_code_key`
* `role_pkey`
* `user_account_email_key`
* `user_account_pkey`
* `user_account_username_key`
* `pk_user_role`

These correspond to the V3 primary-key and UNIQUE constraints.

No additional foreign-key indexes were found.

No additional indexes were added during verification.

Whether additional application/query-performance indexes are required will be assessed when the actual V3 application query patterns and repository implementations are developed.

Result:

**V3 index verification — PASS**

---

## 11. Current V3 Database Status

As of 2026-08-27, the V3 database migration and physical PostgreSQL implementation are verified.

### V3 Database Verification Summary

* V3 migration execution — PASS
* Flyway history — PASS
* Six V3 tables — PASS
* Thirty V3 columns — PASS
* Data types / VARCHAR lengths / nullability — PASS
* Primary keys — PASS
* Composite primary key — PASS
* UNIQUE constraints — PASS
* Six foreign keys — PASS
* ON DELETE behavior — PASS
* ON UPDATE behavior — PASS
* V3 indexes — PASS

### Current conclusion

**V3 DATABASE MIGRATION AND PHYSICAL DATABASE IMPLEMENTATION: VERIFIED**

No V3 SQL changes were required during this verification.

---

## 12. V3 Application Implementation Status

Important: V3 application implementation has NOT yet started.

The following have not yet been implemented:

### Java / Spring Boot

* V3 Java entities/models
* V3 repositories
* V3 service layer
* V3 controllers
* V3 request/response DTOs
* V3 validation
* V3 security/authorization implementation
* V3 application-level business logic
* V3 backend automated tests
* V3 API tests

### React / Frontend

* V3 React pages
* V3 React components
* V3 forms
* V3 frontend validation
* V3 API integration
* V3 authentication/identity UI
* V3 citizen UI
* V3 browser functionality

Therefore, V3 must NOT yet be described as fully implemented at the application level.

The verified status is specifically:

**Database layer: VERIFIED**

**Backend application layer: NOT YET IMPLEMENTED**

**Frontend application layer: NOT YET IMPLEMENTED**

---

## 13. Remaining V3 Work

The next phase is V3 application implementation.

Planned sequence:

1. Review the existing V1/V2 Java and React structure.
2. Implement V3 Java/Spring Boot backend classes according to Part 2A/2B.
3. Implement V3 repositories and database integration.
4. Implement V3 service/business logic.
5. Implement V3 APIs/controllers.
6. Implement validation and required security/authorization boundaries.
7. Create backend automated tests.
8. Implement V3 React/frontend functionality.
9. Integrate frontend with V3 APIs.
10. Verify V3 functionality through the browser.
11. Perform V1/V2 regression verification.
12. Review Git status and diff.
13. Commit V3 only after all required verification passes.

---

## 14. Git Status

V3 has NOT yet been committed.

Git review and commit are intentionally deferred until the V3 application implementation and verification are complete.

---

## 15. Current Overall V3 Status

As of 2026-08-27:

**V3 database migration:** COMPLETE

**V3 database physical verification:** COMPLETE / PASS

**V3 Java implementation:** NOT STARTED

**V3 React implementation:** NOT STARTED

**V3 automated tests:** NOT STARTED

**V3 browser verification:** NOT STARTED

**V1/V2 regression verification:** PENDING

**Project-context.md:** This section records the V3 verification delta and current state.

**Git commit:** PENDING

### Authoritative status

> V3 database foundation is implemented and fully verified locally. V3 application-layer implementation (Java/Spring Boot and React) has not yet started. No V3 commit should be made until application implementation, automated testing, browser verification, and V1/V2 regression checks are completed.

### V3 Persistence Integration Verification — COMPLETE

The first PostgreSQL repository integration tests were executed successfully against the configured local database.

Executed:

`.\gradlew clean test`

Gradle result:

`BUILD SUCCESSFUL`

Test report verification confirmed:

- CivicTransparencyApiApplicationTests — 1 test, 0 failures, 0 errors, 0 skipped
- RoleRepositoryIntegrationTest — 1 test, 0 failures, 0 errors, 0 skipped
- UserAccountRepositoryIntegrationTest — 1 test, 0 failures, 0 errors, 0 skipped

Total:

- Tests executed: 3
- Passed: 3
- Failures: 0
- Errors: 0
- Skipped: 0

This confirms that the Spring Boot application context loads successfully and that the initial V3 repository integration tests execute successfully against the configured local PostgreSQL environment.

### V3 End-of-Checkpoint Status

V3 Flyway migration: COMPLETE and successfully applied.

V3 physical database verification: COMPLETE.

V3 JPA entity mapping: COMPLETE.

V3 repository layer: COMPLETE.

Initial V3 persistence integration verification: COMPLETE.

V3 service/business layer: NOT YET IMPLEMENTED.

REST APIs/controllers: NOT YET IMPLEMENTED.

Security/authentication/authorization: NOT YET IMPLEMENTED.

React/frontend functionality: NOT YET IMPLEMENTED.

V1/V2 application-level regression verification: PENDING.

Git review and V3 commit: PENDING.

The project remains intentionally at the persistence-foundation stage. No functional Java service/controller implementation and no React application functionality has been developed yet.

Next continuation point:
- Extend persistence integration testing to the remaining V3 relationships, particularly UserRole, CitizenProfile, CitizenContact and CitizenAddress.
- Then proceed to the service/business layer only after persistence verification is satisfactory.

# Project Context — V3 Verification Delta

## Addendum / Update after V3 Implementation and Verification

**Date:** 28-Aug-2026

### V3 Implementation Status

V3 database migration and the corresponding Java persistence/application layer have now been implemented and verified.

#### Database / Flyway

* V3 migration:

  * `V3__create_identity_and_citizen_tables.sql`
* V3 was successfully applied to the local PostgreSQL database.
* Flyway successfully validated and applied migrations V1, V2 and V3.
* `spring.jpa.hibernate.ddl-auto=validate` remains enabled, so Hibernate validates the Java entity mappings against the database schema rather than modifying the schema.

#### V3 Identity Entities

Implemented under:

`com.civictransparency.identity.entity`

* `Role`
* `UserAccount`
* `UserRole`
* `UserRoleId`

Repositories:

`com.civictransparency.identity.repository`

* `RoleRepository`
* `UserAccountRepository`
* `UserRoleRepository`

#### V3 Citizen Entities

Implemented under:

`com.civictransparency.citizen.entity`

* `CitizenProfile`
* `CitizenAddress`
* `CitizenContact`

Repositories:

`com.civictransparency.citizen.repository`

* `CitizenProfileRepository`
* `CitizenAddressRepository`
* `CitizenContactRepository`

Key design points:

* `CitizenProfile` is linked one-to-one with `UserAccount`.
* `CitizenContact` uses a shared primary key with `CitizenProfile` through `@MapsId`.
* `CitizenAddress` supports multiple addresses per citizen and a primary-address indicator.
* Citizen contact and address values are represented as encrypted fields where applicable.
* Citizen identity/contact information is kept separate from publicly visible citizen/complaint information in accordance with the privacy-oriented architecture.

### V3 DTO / Service Layer

Citizen DTOs and services were added/updated to align with the V3 persistence model.

Important DTO alignment corrections made during verification:

`CreateCitizenAddressRequest`

* `encryptedAddressText`
* `cityId`
* `postalCode`
* `primary`

`CreateCitizenContactRequest`

* `encryptedPhone`
* `encryptedEmail`

`CitizenContactResponse`

* `citizenId`
* `verifiedPhone`
* `verifiedEmail`

The response intentionally does not expose encrypted phone/email values.

### Java Compilation Verification

Command executed:

`.\gradlew clean compileJava`

Result:

**BUILD SUCCESSFUL**

All current Java source code compiles successfully after resolving the DTO/service mismatches.

### Test Verification

Command executed:

`.\gradlew clean test`

Final result:

**BUILD SUCCESSFUL**

Verified test classes include:

* `CivicTransparencyApiApplicationTests`
* `RoleRepositoryIntegrationTest`
* `UserAccountRepositoryIntegrationTest`

All tests passed.

The application context successfully initialized, including:

* Spring Boot
* DataSource / HikariCP
* PostgreSQL connection
* Flyway
* Hibernate/JPA
* Entity mappings
* Repository layer

The final test run completed successfully with normal JPA EntityManagerFactory and Hikari connection-pool shutdown messages.

### Configuration Issue Encountered and Resolved

During verification, tests initially failed because the datasource URL supplied through the environment configuration was invalid.

Error:

`'url' must start with "jdbc"`

Root cause:

* `CIVIC_DB_URL` was not being supplied as a valid JDBC PostgreSQL URL to the test process.

The environment configuration was corrected.

Expected URL format:

`jdbc:postgresql://localhost:5432/civic_transparency`

After correcting the environment configuration:

`.\gradlew clean test`

completed successfully.

No application-code workaround was introduced for this configuration issue.

### Current Backend Source Inventory

Current Java source tree includes:

* `CivicTransparencyApiApplication.java`

Identity:

* `identity/entity/Role.java`
* `identity/entity/UserAccount.java`
* `identity/entity/UserRole.java`
* `identity/entity/UserRoleId.java`
* `identity/repository/RoleRepository.java`
* `identity/repository/UserAccountRepository.java`
* `identity/repository/UserRoleRepository.java`

Citizen:

* `citizen/entity/CitizenProfile.java`
* `citizen/entity/CitizenAddress.java`
* `citizen/entity/CitizenContact.java`
* `citizen/repository/CitizenProfileRepository.java`
* `citizen/repository/CitizenAddressRepository.java`
* `citizen/repository/CitizenContactRepository.java`

Citizen DTO/service classes have also been added as part of the V3 application layer.

### Current Verification Position

V3 can now be considered **implemented and technically verified at the compilation, application-context, persistence and repository-integration level**.

However, V3 is **not yet considered fully functionally verified through HTTP/API testing**.

The following still needs to be done:

1. Start the backend with `bootRun`.
2. Verify the V3 REST endpoints using HTTP requests/Postman/curl or equivalent.
3. Verify request validation and response payloads.
4. Verify actual records created in PostgreSQL.
5. Verify citizen/user/role relationships.
6. Verify error handling for invalid requests.
7. Verify privacy-related response behavior.
8. After API verification, proceed to the React/web UI.

### Next Session Starting Point

**Resume from: V3 REST API endpoint verification.**

Do not restart V1/V2/V3 database implementation unless a verification failure identifies a genuine schema/design issue.

The immediate objective is to move from:

`Database + Entities + Repositories + Services + Tests`

to:

`Working REST APIs → API verification → React/Web UI`

The project has now reached the point where the first visible web functionality can be built.

# V3 REST API & Automated Verification Delta — 31-Aug-2026

## 1. V3 Application Verification Progress

Following the V3 persistence implementation, the Citizen Registration REST API and its integration tests were implemented and verified.

The current V3 backend now includes:

* JPA entities
* repositories
* service layer
* registration DTOs
* REST controller
* validation annotations
* global exception handling
* integration tests

The project has therefore progressed beyond the persistence-foundation stage documented in the earlier sections.

---

## 2. Citizen Registration API

Implemented endpoint:

```text
POST /api/v1/citizens/register
```

Controller:

```text
backend/src/main/java/com/civictransparency/citizen/controller/CitizenRegistrationController.java
```

Request DTO:

```text
backend/src/main/java/com/civictransparency/citizen/dto/CitizenRegistrationRequest.java
```

The registration request currently validates:

* email required and valid email format
* username maximum length
* password required and length constrained
* confirm password required and length constrained
* public alias required
* phone required
* address required
* postal code length constraint
* optional city ID

Validation is implemented using Jakarta Bean Validation annotations and `@Valid` on the controller request body.

---

## 3. Registration Integration Tests

Test class:

```text
backend/src/test/java/com/civictransparency/citizen/controller/CitizenRegistrationControllerIntegrationTest.java
```

The registration integration test suite currently contains six tests.

Validation scenarios include:

* successful citizen registration
* missing email
* invalid email
* missing phone
* missing address
* additional registration validation/business scenarios

The test suite executes against the configured local PostgreSQL database and Spring Boot application context.

---

## 4. Memory Constraint During Verification

The development laptop has approximately 5.85 GB of visible physical memory.

During an earlier full integration-test execution, the Java Virtual Machine terminated because native memory allocation failed:

```text
There is insufficient memory for the Java Runtime Environment to continue.
Native memory allocation (malloc) failed
```

The subsequent Gradle communication error:

```text
Connection reset by peer
```

was a consequence of the Java process terminating and was not treated as an application/network defect.

The available physical memory was subsequently checked.

Observed state:

```text
TotalVisibleMemorySize : approximately 5.85 GB
FreePhysicalMemory     : approximately 0.74 GB
```

No excessive Java process remained after the failed execution.

---

## 5. Gradle Memory Configuration

To accommodate the constrained development machine, `backend/gradle.properties` was reduced to:

```properties
org.gradle.jvmargs=-Xmx384m -XX:MaxMetaspaceSize=192m
org.gradle.daemon=true
```

This configuration should be retained unless there is a demonstrated need to change it.

Do not increase JVM/Gradle memory aggressively on the current laptop without first checking available physical memory.

The laptop-first development constraint remains valid.

---

## 6. Registration Validation Issue and Resolution

The initial registration validation tests for invalid/missing fields unexpectedly returned:

```text
HTTP 500
INTERNAL_SERVER_ERROR
```

instead of the expected validation response.

The affected scenarios were:

* invalid email
* missing phone
* missing address

The request DTO correctly contains Jakarta validation annotations, including:

```java
@NotBlank
@Email
@Size(max = 320)
String email
```

and:

```java
@NotBlank
@Size(max = 50)
String phone
```

and:

```java
@NotBlank
@Size(max = 4000)
String address
```

The controller correctly uses:

```java
@Valid @RequestBody CitizenRegistrationRequest request
```

The issue was therefore investigated at the Spring validation/exception-handling boundary rather than by weakening or removing DTO validation.

After the validation/error-handling correction, the complete registration controller integration test passed.

---

## 7. Targeted Test Verification

The following targeted test was executed:

```text
.\gradlew test --tests "*CitizenRegistrationControllerIntegrationTest.shouldRejectMissingEmail"
```

Result:

```text
BUILD SUCCESSFUL
```

---

## 8. Complete Citizen Registration Test Verification

The complete registration controller integration test was then executed:

```text
.\gradlew test --tests "*CitizenRegistrationControllerIntegrationTest"
```

Result:

```text
BUILD SUCCESSFUL
```

All six tests completed successfully.

Result:

```text
6 tests completed
0 failures
0 errors
```

This confirms that the Citizen Registration controller and its current validation/error-handling behavior are functioning correctly under the integration-test environment.

---

## 9. Full Backend Test Verification

The complete backend test suite was executed:

```text
.\gradlew test
```

Result:

```text
BUILD SUCCESSFUL
```

The test execution completed normally, including shutdown of the JPA `EntityManagerFactory` and Hikari connection pools.

This establishes the current backend test baseline as GREEN.

---

## 10. Current V3 Verification Status

Current status:

```text
V3 database migration                  COMPLETE
V3 physical database verification      COMPLETE
V3 JPA entity mapping                  COMPLETE
V3 repository layer                    COMPLETE
V3 service layer                       IMPLEMENTED
V3 DTO layer                            IMPLEMENTED
V3 Citizen Registration API             IMPLEMENTED
V3 validation                           VERIFIED
V3 exception handling                   VERIFIED
V3 registration integration tests       PASS
Full backend automated test suite       PASS
V3 browser verification                 PENDING
V3 API/manual HTTP verification         PENDING
V1/V2 application regression review     PENDING
Git review                              PENDING
V3 Git commit                           PENDING
```
### Local Development Database Configuration Update — 31-Aug-2026

The project no longer requires CIVIC_DB_URL, CIVIC_DB_USERNAME, CIVIC_DB_PASSWORD or CIVIC_ENCRYPTION_KEY to be configured as Windows/PowerShell environment variables for local development.

Local configuration is now maintained in:

backend/src/main/resources/application-local.properties

This file contains the local datasource password and PII encryption key and is explicitly excluded from Git through .gitignore.

application.properties contains the non-secret local datasource URL/username and imports application-local.properties using:

spring.config.import=optional:classpath:application-local.properties

Verification:
- Registration integration tests pass with the environment variables present.
- Registration integration tests also pass from a fresh PowerShell session without the CIVIC_DB_* environment variables.

Therefore local Spring Boot startup/test configuration is independent of the Windows environment-variable configuration.

Secrets remain excluded from Git.
---

## 11. Important Current Checkpoint

The project should **not** return to V3 database implementation unless a genuine verification failure identifies a schema/design problem.

The current work has moved from:

```text
Database
→ Entities
→ Repositories
→ Services
→ DTOs
→ REST Controller
→ Automated Verification
```

to:

```text
REST/API Manual Verification
→ Browser Verification
→ V1/V2 Regression
→ Git Review
→ V3 Commit
```

The immediate next objective is therefore to verify the actual HTTP behavior of:

```text
POST /api/v1/citizens/register
```

including:

1. successful registration
2. validation failures
3. response payload
4. HTTP status codes
5. records created in PostgreSQL
6. UserAccount ↔ CitizenProfile relationship
7. CitizenContact persistence
8. CitizenAddress persistence
9. role assignment behavior
10. privacy of returned data

After API verification, browser/UI verification should proceed.

---

## 12. Project Continuity Note

The earlier sections of this document contain historical checkpoints and should not be rewritten merely to remove their previous status statements.

This section supersedes those earlier status statements where they conflict with the current implementation state.

The authoritative current position is:

> **V3 backend application implementation is implemented and automated-test verified. The current remaining work is final browser regression, production hardening, and the final Git review/commit.**

The memory limitation of the development laptop is an environment constraint and must continue to be considered when selecting Gradle/JVM test commands.


## V4 — Citizen Registration API + Initial Web UI Verification

### V4 implementation completed

V4 extends the V3 citizen registration foundation into a working end-to-end registration flow.

#### Backend implementation

Implemented the citizen registration API:

* `POST /api/v1/citizens/register`
* Registration creates:

  * `identity.user_account`
  * `identity.user_role`
  * `citizen.citizen_profile`
  * `citizen.citizen_contact`
  * `citizen.citizen_address`
* Default account/profile status:

  * User account: `ACTIVE`
  * Citizen profile: `ACTIVE`
* Default role:

  * `CITIZEN`

Registration response contains:

* `citizenId`
* `userId`
* `username`
* `publicAlias`
* `profileStatus`
* `userStatus`
* `roleCode`

#### Security / PII handling

Verified that:

* Password is hashed using the password-hashing service and is not stored as plaintext.
* Phone number is encrypted before persistence.
* Email is encrypted before persistence in `citizen_contact`.
* Address is encrypted before persistence in `citizen_address`.
* Citizen public alias is stored as non-sensitive profile information.
* Citizen address remains separate from the public profile.

#### Validation / exception handling

Implemented centralized exception handling through:

`common.exception.GlobalExceptionHandler`

Handles:

* `InvalidRegistrationDataException`
* `MethodArgumentNotValidException`
* Unexpected exceptions

Validation errors currently return the standardized structure:

* HTTP `400`
* code: `INVALID_REGISTRATION_DATA`
* message: `The registration details provided are invalid.`
* request path included in response

Invalid email validation was specifically verified to produce:

`MethodArgumentNotValidException`

and the expected HTTP 400 response.

#### Role assignment

Citizen registration retrieves the seeded `CITIZEN` role and assigns it through:

* `RoleService`
* `UserRoleService`
* `UserRoleRepository`

`UserRole` uses the composite `UserRoleId` with JPA `@EmbeddedId` / `@MapsId`.

### Database verification

V4 database/reference data is present through:

`V4__seed_identity_reference_roles.sql`

The `CITIZEN` role was verified in PostgreSQL and successfully assigned to a newly registered user.

A complete HTTP registration was manually tested against the running application.

The returned registration response contained valid:

* citizen UUID
* user UUID
* generated username
* public alias
* `ACTIVE` profile status
* `ACTIVE` user status
* `CITIZEN` role

Database verification confirmed:

* user account created
* password stored as a BCrypt-style hash
* citizen profile created
* citizen role assigned
* phone encrypted
* email encrypted
* address encrypted
* postal code stored
* address marked primary

### Automated test verification

`CitizenRegistrationControllerIntegrationTest` is passing.

Command used:

`.\gradlew cleanTest test --tests "*CitizenRegistrationControllerIntegrationTest"`

Result:

* 6 tests completed
* 0 failures
* 0 errors
* `BUILD SUCCESSFUL`

The integration test covers:

* successful registration
* password mismatch
* missing email
* invalid email
* missing phone
* missing address

### Local configuration change

Removed the dependency on database/environment variables for normal local application configuration.

Current approach:

`src/main/resources/application.properties`

contains the non-secret local database URL and username, while secrets are loaded from:

`src/main/resources/application-local.properties`

The local file contains:

* database password
* PII encryption key

`application-local.properties` is explicitly excluded from Git through `.gitignore`.

Current import:

`spring.config.import=optional:file:./application-local.properties`

This allows local development without requiring `CIVIC_DB_URL`, `CIVIC_DB_USERNAME`, `CIVIC_DB_PASSWORD`, and `CIVIC_ENCRYPTION_KEY` to be manually configured as PowerShell environment variables.

Important: the local secrets file must remain uncommitted.

### Application / HTTP verification

Application successfully starts on:

`http://localhost:8080`

Spring Boot detects and serves:

`src/main/resources/static/index.html`

Health endpoint verified:

`GET /actuator/health`

Response status:

`UP`

The application successfully served the static landing page and the registration API was successfully invoked using PowerShell `Invoke-RestMethod`.

### Initial Web UI

A static landing page has been added under:

`src/main/resources/static/index.html`

Current page includes:

* Civic Transparency branding
* Hero section
* "Make your city better." messaging
* Report an issue action
* Public dashboard action
* Issue category presentation:

  * Potholes
  * Roads
  * Encroachments
  * Schools
  * Sanitation
  * Public safety

The current landing page is still an initial/static UI.

### Current state / next step

Backend V3/V4 citizen registration is verified end-to-end.

The next implementation step is to connect the existing web UI to the citizen registration functionality.

Target flow:

`Landing Page`
→ `Citizen Registration UI`
→ `POST /api/v1/citizens/register`
→ `User Account + Role + Citizen Profile + Contact + Address`
→ `Registration Success`

After registration UI integration, continue with login/authentication and subsequent citizen-facing functionality.

### Git status at V4 checkpoint

Modified:

* `build.gradle`
* `gradle.properties`
* `src/main/resources/application.properties`
* `../docs/project-context/PROJECT_CONTEXT.md`
* `.gitignore`

New implementation areas include:

* `src/main/java/com/civictransparency/citizen/`
* `src/main/java/com/civictransparency/common/`
* `src/main/java/com/civictransparency/identity/`
* `src/main/java/com/civictransparency/security/`
* `src/main/resources/db/migration/V3__create_identity_and_citizen_tables.sql`
* `src/main/resources/db/migration/V4__seed_identity_reference_roles.sql`
* `src/main/resources/static/`
* `src/test/java/com/civictransparency/citizen/`
* `src/test/java/com/civictransparency/identity/`

## V3 Registration — Frontend Integration, CORS & Verification Delta

### Frontend Initialization

* Created frontend under:
  `C:\Kareem\Code\CivicTransparency\frontend`
* Frontend stack:

  * Next.js 16.3.4
  * TypeScript
  * Tailwind CSS
  * App Router
  * Node.js 24.20.0
  * npm 11.4.2
* Next.js application created using `create-next-app@16.3.4`.
* Frontend linting verified successfully with `npm run lint`.
* Login UI is present but authentication/security implementation is still pending.

### Citizen Registration UI

Implemented the citizen registration UI in `AuthPanel.tsx`.

Registration currently supports:

* Email
* Optional username
* Password
* Confirm password
* Public alias
* Phone
* Address
* State
* City
* Optional postal code

UI behavior:

* Required fields are marked with a red `*`.
* Optional fields are explicitly labelled `(optional)`.
* Browser-native required-field and email validation is active.
* Password/confirm-password mismatch is validated before submission.
* State list is loaded dynamically from the backend.
* City list is loaded dynamically based on the selected state.
* City selection is reset when the state changes.
* Registration success/error messages are displayed in the UI.
* Registration panel supports vertical scrolling.

### Geography Integration

Frontend consumes:

* `GET /api/v1/geography/states`
* `GET /api/v1/geography/states/{stateId}/cities`

Current seeded geography used for E2E verification:

* Karnataka — code `KA`
* Bengaluru — code `BLR`

### CORS

Added:

`backend/src/main/java/com/civictransparency/common/config/CorsConfig.java`

Configured backend CORS for the local frontend:

* Origin: `http://localhost:3000`
* API mapping: `/api/**`
* Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
* Headers: all

This resolved browser cross-origin access between the Next.js frontend (`localhost:3000`) and Spring Boot backend (`localhost:8080`).

### Citizen Registration API

Registration endpoint:

`POST /api/v1/citizens/register`

Current registration response contains:

* `citizenId`
* `userId`
* `username`
* `publicAlias`
* `profileStatus`
* `accountStatus`
* `roleCode`

Backend registration flow:

1. Validate registration DTO.
2. Validate password confirmation.
3. Check duplicate email.
4. Check duplicate username when supplied.
5. Hash password.
6. Create user account.
7. Assign `CITIZEN` role.
8. Create citizen profile.
9. Encrypt/store contact information.
10. Encrypt/store address information.
11. Return registration response.

### Duplicate Registration Handling

Previously duplicate email/username checks threw `IllegalArgumentException`, which resulted in HTTP 500 through the generic exception handler.

Implemented:

`DuplicateRegistrationException`

and corresponding `GlobalExceptionHandler` handling.

Duplicate registration now returns:

* HTTP `409 CONFLICT`
* Code: `DUPLICATE_REGISTRATION`

Messages:

* Duplicate email: `An account with this email already exists.`
* Duplicate username: `This username is already in use.`

An automated integration test was added for duplicate email handling.

### Invalid City Validation

During negative testing, an invalid/non-existent `cityId` initially resulted in:

* PostgreSQL foreign-key violation
* HTTP `500 INTERNAL SERVER ERROR`

Database verification confirmed that the failed transaction did not create a user account.

The address service was then enhanced to validate the supplied city before attempting the address insert using the existing `CityRepository.existsById()`.

Current behavior:

* `cityId = null` → allowed
* Existing city UUID → allowed
* Non-existent city UUID → HTTP `400 BAD REQUEST`
* Code: `INVALID_REGISTRATION_DATA`

The database foreign-key constraint remains as the final integrity safeguard.

### Successful End-to-End Registration Verification

Verified through the browser UI:

* Karnataka displayed in the state dropdown.
* Bengaluru displayed after selecting Karnataka.
* Registration completed successfully.
* UI displayed:

  * `Registration successful.`
  * `Welcome, E2E Test Citizen.`
  * `Your account has been created successfully.`

Database verification confirmed creation of:

* `identity.user_account`
* `citizen.citizen_profile`
* `citizen.citizen_address`

The citizen address was correctly associated with the seeded Bengaluru city.

### Negative / Boundary UI & API Verification

The following registration scenarios have been tested successfully:

1. Password mismatch — browser/UI validation passed.
2. City not selected — browser required-field validation passed.
3. Invalid email — browser email validation passed.
4. Short password — backend validation returned `INVALID_REGISTRATION_DATA`.
5. Duplicate email — HTTP 409 / `DUPLICATE_REGISTRATION`.
6. Missing required field — browser required-field validation passed.
7. Duplicate username — HTTP 409 / `DUPLICATE_REGISTRATION`.
8. Optional username and postal code blank — registration succeeded.
9. Non-existent `cityId` through API — HTTP 400 / `INVALID_REGISTRATION_DATA`.
10. Invalid-city transaction — verified that no partial `identity.user_account` row was created.

### Automated Verification Status

After the registration changes:

`.\gradlew clean test`

completed successfully.

Latest result:

* `BUILD SUCCESSFUL`
* 5 actionable tasks executed
* Existing automated tests continue to pass.

Frontend linting also passes with:

`npm run lint`

### Current V3 Status

Citizen registration is now verified across:

`Next.js UI → CORS → Spring Boot Controller → DTO Validation → Registration Service → Identity/Citizen Services → PostgreSQL → Transaction/Rollback`

The registration flow is considered functionally verified for the currently implemented scope.

Authentication/login, session/token security, authorization, and subsequent citizen issue-reporting functionality remain to be implemented and verified.


## Current Project Checkpoint — Complaint Submission Foundation

**Date:** 15-Sep-2026

This section supersedes earlier status statements where they conflict with the current repository state. Historical checkpoint sections above are retained for continuity.

### Current Git checkpoint

Current branch and commit:

```text
master
769f1a6 Implement Complaint Submission flow with V8-V11 migrations
```

The working tree was clean before this documentation update.

Recent completed milestones:

* `545ca21` — citizen registration, authentication and Next.js integration.
* `49d0581` — citizen dashboard and post-login redirect.
* `7c652f6` — complaint categories, geography hierarchy APIs and category/location selection UI.
* `769f1a6` — complaint submission foundation, location/evidence persistence and workflow initialization.

### Database and Flyway status

Flyway migrations currently present are:

```text
V1  create application schemas                         COMPLETE
V2  create geography tables                            COMPLETE
V3  create identity and citizen tables                COMPLETE
V4  seed identity reference roles                      COMPLETE
V5  seed geography reference data                      COMPLETE
V6  create and seed complaint categories               COMPLETE
V7  seed Bengaluru wards and localities                 COMPLETE
V8  create complaint tables                             COMPLETE
V9  create location tables                              COMPLETE
V10 create workflow tables                              COMPLETE
V11 create evidence tables                              COMPLETE
V12 transparency projection                             NOT STARTED
V13 analytics                                          NOT STARTED
V14 administration                                     NOT STARTED
```

The V8-V11 schema foundation currently includes:

* Complaint records and category/subcategory references in the `complaint` schema.
* Exact operational location, exposure policy and public geometry fields in `location.complaint_location`.
* Complaint lifecycle state and transition history in `workflow.workflow` and `workflow.workflow_transition`.
* Evidence metadata and media moderation records in `evidence.evidence` and `evidence.media_moderation`.
* PostGIS geography/geometry columns and GIST indexes for location queries.

The V6/V7 reference data provides complaint categories/subcategories and seeded Bengaluru wards/localities. The category and location design is documented in [Category_and_Location_Selection_Design_Plan.md](../../docs/design/Category_and_Location_Selection_Design_Plan.md). The frozen architecture and database design remain authoritative; these implementation notes do not change them.

### Implemented backend application surface

The current Spring Boot modular monolith contains these verified application areas:

* Identity and citizen registration, including BCrypt password hashing, PII encryption, role assignment and validation/error handling.
* Authentication through `POST /api/v1/auth/login`, accepting an email or username and returning a bearer-style HMAC access token, expiry, public alias and roles.
* Geography read APIs:
  * `GET /api/v1/geography/states`
  * `GET /api/v1/geography/states/{stateId}/cities`
  * `GET /api/v1/geography/cities/{cityId}/wards`
  * `GET /api/v1/geography/wards/{wardId}/localities`
* Category read API: `GET /api/v1/categories`.
* Complaint submission through `POST /api/v1/complaints`.

The current complaint submission service creates the complaint, derives severity from the selected category/subcategory, creates the initial `SUBMITTED` workflow, persists optional location data, and persists evidence metadata in one transactional operation. The controller returns HTTP `201` with the generated `complaintId`.

The current complaint request includes citizen/category/geography identifiers, description, optional title/subcategory, optional latitude/longitude/accuracy/description, and evidence metadata. Actual multipart upload and object-storage integration are not implemented yet; the current web flow submits mock evidence metadata.

### Implemented frontend application surface

The Next.js frontend currently includes:

* `/` — combined landing, registration and login experience.
* `/dashboard` — session-backed citizen portal with sign-out and report-issue navigation.
* `/report` — category, subcategory, city, ward and locality selection flow.
* `/report/details` — complaint description and simulated evidence submission form.

The frontend uses Next.js `16.3.4`, React `19.2.8`, TypeScript, Tailwind CSS and the App Router. It calls the local Spring Boot API at `http://localhost:8080` and uses the configured local CORS policy for browser access.

Current frontend limitations:

* The complaint form derives the citizen UUID from the successful login session and forwards the bearer token. Complaint creation and citizen complaint retrieval validate the signed token; broader HTTP authentication coverage remains pending.
* The selected city is currently seeded to Bengaluru in the report flow.
* File selection is visual only; no file bytes are uploaded and evidence metadata is simulated.
* Dashboard complaint counts and recent reports now load from the authenticated citizen complaint API; public metrics remain placeholders and transparency/analytics APIs are not implemented.
* Token storage currently uses browser `sessionStorage`; production-grade token lifecycle, refresh/revocation and route/API authorization remain pending.

### Automated verification status

The repository contains integration tests for:

* Spring Boot application context loading.
* Identity and role repositories.
* Citizen registration and validation/error cases.
* Authentication by email and username, invalid-password handling and token issuance.
* Geography hierarchy APIs.
* Category APIs.
* Atomic complaint submission, including complaint, location, workflow and evidence persistence.

Previously recorded backend verification for the registration/authentication/category/geography slices was green before the V8-V11 implementation. The latest complaint implementation includes `ComplaintControllerIntegrationTest`, designed to verify HTTP `201`, generated complaint ID, derived severity, complaint reference, location persistence, initial `SUBMITTED` workflow state and evidence metadata persistence.

Current verification result on 15-Sep-2026:

* The targeted `ComplaintControllerIntegrationTest` passes after aligning the audit JSONB mapping and correcting the complaint fixture to use the seeded category/subcategory IDs.
* The full backend suite passes with `backend\\gradlew.bat test --no-daemon`.
* The evidence `sha256` PostgreSQL `CHAR(64)` mapping mismatch is fixed with an explicit Hibernate `CHAR` JDBC type.
* Frontend lint and the Next.js production build pass after the authenticated identity handoff, complaint dashboard, and route Suspense fixes. Full browser regression verification remains pending.

Local development secrets belong only in the ignored `backend/application-local.properties` file or a secret manager. Do not stage or commit local secrets. Production must use managed secrets rather than local development values.

## Check-in readiness — 2026-10-18

The current implementation has passed the full backend test suite, frontend lint, frontend production build, and `git diff --check`. The main functional loop is implemented: citizens submit and track their complaints; system administrators configure authorities, departments, staff accounts, and routing; department staff review their assigned queue and record status transitions; optional department email is disabled unless explicitly configured.

Before a manual end-to-end smoke test, provision the first `SYSTEM_ADMIN` account securely, configure at least one active authority/department/staff user and a matching routing rule, then test with both a citizen and authority account. Email delivery additionally requires a local SMTP capture service and department-recipient configuration. These are operational/manual verification items, not missing login or portal screens.

No commit has been created. Review the complete worktree before staging because it contains multiple modified and untracked files; ensure ignored local secrets remain untracked.

### Authoritative current position

```text
Architecture and detailed design                 COMPLETE / FROZEN
Database migrations V1-V17                       PREVIOUSLY APPLIED / VERIFIED
Database migration V18                            CREATED; VALIDATED BY BACKEND TEST SUITE (2026-10-18)
Identity/citizen registration                     IMPLEMENTED and verified
Authentication/login                              IMPLEMENTED and integration-tested
Category/geography selection                      IMPLEMENTED and integration-tested
Complaint submission foundation                   IMPLEMENTED and integration-tested
Workflow initialization                           IMPLEMENTED for initial SUBMITTED state
Location/evidence metadata persistence            IMPLEMENTED
Public transparency projection                    NOT STARTED
Analytics and general administration              NOT STARTED (workflow configuration APIs exist)
Authority/routing/SLA configuration APIs           IMPLEMENTED; BACKEND TESTS GREEN
Automatic routing and assignment                  IMPLEMENTED; BACKEND TESTS GREEN
Manual assignment/reassignment history            IMPLEMENTED; BACKEND TESTS GREEN
Workflow transitions and authority scope          IMPLEMENTED; BACKEND TESTS GREEN
SLA pause/resume/resolve                           IMPLEMENTED; BACKEND TESTS GREEN
Authority assigned-complaint queue UI/API         IMPLEMENTED; DEPARTMENT-SCOPED
Authority staff sign-in and admin provisioning UI  IMPLEMENTED; FIRST SYSTEM_ADMIN BOOTSTRAP REQUIRED
Citizen complaint status/history                  IMPLEMENTED; OWNER-SCOPED
Optional department assignment email              IMPLEMENTED; DISABLED BY DEFAULT
Real media upload/moderation pipeline              NOT STARTED
Production authorization/token lifecycle           PARTIAL (complaint APIs validate signed bearer tokens)
Current backend test suite                         GREEN (2026-10-18)
Frontend lint verification                         GREEN (`npm run lint`)
Frontend production build verification            GREEN (`npm run build`)
Full browser regression verification               PENDING (requires bootstrapped test authority and citizen)
Live SMTP capture verification                      PENDING (optional email disabled by default)
```

### Active issue tracker

1. Issue 01 — Hibernate/PostgreSQL type mismatch for evidence hash
   - Root cause: `evidence.evidence.sha256` is defined as `CHAR(64)` in PostgreSQL, while the JPA entity mapped it with a generic string type that did not align with PostgreSQL's `bpchar` representation during schema validation.
   - Fix approach: align the JPA `@Column` mapping to `char(64)` and rerun the backend validation tests.
  - Status: FIXED / VERIFIED

2. Issue 02 — Java compilation failure in notification integration test lambda capture
   - Root cause: the lambda in `NotificationServiceIntegrationTest` captured mutable loop/assignment variables that were not effectively final.
   - Fix approach: store the expected UUID values in final local variables and assert against those values inside the lambda.
  - Status: FIXED / VERIFIED by full backend suite

3. Issue 03 — Hibernate schema validation still expects VARCHAR for evidence.sha256
   - Root cause: Hibernate is defaulting the mapped String field to VARCHAR even when the database column is defined as `CHAR(64)`; the fix requires an explicit JDBC type code for `CHAR`.
   - Fix approach: annotate the field with `@JdbcTypeCode(SqlTypes.CHAR)` and validate again.
   - Status: FIXED / VERIFIED

4. Issue 04 — Test fixture uses non-existent complaint category/subcategory IDs
   - Root cause: the complaint controller integration test used placeholder IDs instead of the UUIDs seeded in V6, so the request resolved to `null` subcategory and defaulted to `MEDIUM` severity.
   - Fix approach: update the integration test to use the actual seeded `a100...` category and `b100...` subcategory UUIDs from the Flyway migration.
  - Status: FIXED / VERIFIED by `ComplaintControllerIntegrationTest`

5. Issue 05 — Audit JSONB payload mapping rejected complaint submission
  - Root cause: `audit.audit_event.payload` is PostgreSQL `JSONB`, while the JPA String field was bound as a character/varchar value.
  - Fix approach: annotate the field with `@JdbcTypeCode(SqlTypes.JSON)` so Hibernate binds it as JSON.
  - Status: FIXED / VERIFIED by `ComplaintControllerIntegrationTest` and the full backend suite

### Recommended next sequence

1. Establish the first `SYSTEM_ADMIN` account through the secure bootstrap process, then configure active authority users, departments, and routing rules.
2. Complete browser end-to-end regression using citizen and authority accounts; verify department-isolated queues and citizen-owned status timelines.
3. Configure a local SMTP capture service and department recipient map to verify optional assignment email delivery.
4. Complete the remaining production hardening items: token lifecycle and real evidence upload/moderation.
5. Implement transparency projection and analytics only through their designated read models.
6. Review the complete worktree, verify no local secrets are staged, then stage and commit the intended changes. Browser and SMTP manual checks remain pre-release verification items.

**End of Project Context**
