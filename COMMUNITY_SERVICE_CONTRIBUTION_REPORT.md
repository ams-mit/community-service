# AMS Community Service — Contribution & Change Report

**Project**: Apartment Management System (AMS) — Community Microservice  
**Repository**: [ams-mit/community-service](https://github.com/ams-mit/community-service.git)  
**Date**: September 26, 2026  
**Primary Focus**: Microservice Architecture, Cloud Database Integration, CI/CD Pipeline, & Frontend UI  

---

## 1. Executive Summary & Team Contribution Matrix

| Contributor | GitHub Identity / Email | Commits | Primary Responsibilities & Focus Area |
| :--- | :--- | :---: | :--- |
| **Kaveesha Vimukthi** *(You)* | `Kaveesha-V` / `kaveeshavimukthiai@gmail.com` | **11** | Canonical APIs (`COMM-001`–`COMM-012`), Notification Engine, CI/CD bug fix, Aiven MySQL Cloud integration, Full React UI |
| **Sajana Jayawardhana** | `SajanaJayawardhana` | **15** | Initial Spring Boot setup, base CRUD endpoints, Postman collection, initial booking & visitor foundations |
| **sanduDM** | `sanduDM` | **2** | Containerization, multi-stage `Dockerfile`, and `.dockerignore` |
| **imChamikara** | `imChamikara` | **2** | GitHub Actions Maven CI pipeline definition |
| **DIKKUMBURA D.K.C.P.** | `DIKKUMBURA` | **1** | Pull Request review & merge management (PR #8) |

---

## 2. Detailed Breakdown: What You (Kaveesha Vimukthi) Did

You led the **architectural expansion, cloud database enablement, test automation repair, and the full end-to-end frontend experience**.

### 2.1 Implementation of Full Canonical API Suite (`COMM-001` to `COMM-012`)
*Commit `7760bc3` (+2,557 lines across 51 files)*
- **Complete Domain Specifications**: Implemented the standardized canonical API specification documented in `COMMUNITY_SERVICE_CANONICAL_API_REGISTRY.md`.
- **Facilities Subsystem**:
  - Implemented dynamic availability calculation (`GET /api/v1/facilities/{id}/availability?date=YYYY-MM-DD`).
  - Added operating hours time-slot generation in `FacilityService.java`.
- **Booking Subsystem**:
  - Implemented reservation cancellation endpoint (`PATCH /api/v1/bookings/{id}/cancel`).
  - Added resident reservation history lookup (`GET /api/v1/bookings/user/{userId}`).
  - Added status-based filtering (`GET /api/v1/bookings?status=CONFIRMED`).
- **Visitor Subsystem**:
  - Added visitor pass checkout endpoint (`PATCH /api/v1/visitors/{id}/checkout`).
  - Added security pass verification and history filtering in `VisitorService.java`.
- **Announcements Subsystem**:
  - Added notice update (`PUT /api/v1/announcements/{id}`), archival (`PATCH /api/v1/announcements/{id}/archive`), and deletion in `AnnouncementService.java`.
- **Spring Actuator Health Endpoints (`COMM-011`, `COMM-012`)**:
  - Configured `/actuator/health` and `/actuator/info` in `application.properties`.
- **Swagger / OpenAPI Documentation**:
  - Integrated `springdoc-openapi-starter-webmvc-ui` in `pom.xml` exposing `/swagger-ui.html` and `/v3/api-docs`.

### 2.2 Built-In Notification Engine
*Engineered from the ground up by Kaveesha*
- Created `Notification.java` and `NotificationService.java`.
- Hooked real-time event triggers into booking confirmations, visitor check-ins, and emergency announcements.
- Added comprehensive unit testing in `NotificationServiceTest.java`.

### 2.3 CI/CD Surefire Test Failure Investigation & Fix
*Commits `7d7dcdf`, `bad6899`, `191a007`, `d782757`, `51d915f`*
- **The Problem**: GitHub Actions build was failing with:
  > `Driver org.h2.Driver claims to not accept jdbcUrl, jdbc:mysql://127.0.0.1:3307/community_service_db`
- **Root Cause Identified**: The CI workflow injected MySQL environment variables (`SPRING_DATASOURCE_URL`) that collided with Spring's context test requiring an in-memory database.
- **Your Solution**:
  - Hardened `CommunityServiceApplicationTests.java` with explicit in-memory H2 configuration via `@SpringBootTest(properties = {...})`.
  - Added `src/test/resources/application-test.properties`.
  - Updated `.github/workflows/maven-ci.yml` to pass cleanly with all 13 unit/integration tests passing.

### 2.4 Aiven Cloud MySQL Database Migration & Live Schema Initialization
*Commit `6c85094` on branch `error-fix`*
- Connected the microservice to **Aiven Cloud MySQL** (`mysql-744796c-kaveeshavimukthiai-6aca.f.aivencloud.com:21365/defaultdb`).
- Executed live Hibernate DDL to create database tables on Aiven:
  - `facilities`, `bookings`, `visitors`, `announcements`, `notifications`.
- Automatically seeded initial community amenity records (Clubhouse, Olympic Pool, Tennis Court, Lounge, Gym, BBQ) and system notifications into the cloud database.
- Configured `application-mysql.properties` with SSL mode (`REQUIRED`), Hikari pool settings, and `MySQLDialect`.

### 2.5 Security Best Practices & Git Secret Protection
- GitHub Push Protection blocks commits containing live Aiven service passwords (`AVNS_...`).
- Parameterized credentials via `${SPRING_DATASOURCE_PASSWORD:}`.
- Added `.env` (git-ignored) for local developer execution.
- Added `.env.example` and updated `.gitignore` to protect credentials from public repository leakage.

### 2.6 Full React + TypeScript Web Application
*Built in `community-service-ui` (Kept independent from backend repo per team guidelines)*
- **Facilities & Bookings**: Browse amenities, filter by type, view real-time availability slots, book reservations, and cancel bookings.
- **Visitor Pass Hub**: Pre-register visitors, generate QR codes, pass status badges, check-in and checkout controls.
- **Announcements**: Interactive notice bulletin with role-based visibility, priority tags, and publish modal.
- **Notification Drawer**: Real-time alerts with unread counter and direct workflow routing.
- **Dashboard**: High-level KPI metrics, quick actions, and recent activity timeline.

---

## 3. Detailed Breakdown: What Your Team Members Did

### 3.1 Sajana Jayawardhana (Project Scaffolding & Initial CRUD)
*15 commits between 2026-09-12 and 2026-09-15*
- **Initial Setup**: Created the original Spring Boot Maven project structure (`315f4b6`, `4144ef8`).
- **Base Facilities Endpoint**: Implemented initial `GET /api/v1/facilities` (`fe81037`).
- **Base Booking Creation**: Created initial booking model and facility conflict validation (`8b82eab`, PR #1).
- **Booking Status Update**: Added endpoint to update booking status to `CONFIRMED` or `CANCELLED` (`5ad11cd`, PR #2, #3, #4).
- **Base Visitor Registration**: Added visitor registration and check-in endpoints (`b10866b`, PR #5).
- **Base Announcements**: Added announcement publishing and role filtering (`30c9db6`, PR #6).
- **Postman Collection**: Created `AMS_Community_Service.postman_collection.json` (`507f8be`).

### 3.2 sanduDM (Docker Containerization)
*2 commits on 2026-09-25 (PR #10)*
- **Dockerfile**: Created multi-stage `Dockerfile` using `eclipse-temurin:21-jdk-alpine` for the build stage and `eclipse-temurin:21-jre-alpine` for production runtime (`04f05f8`).
- **Security & Port Mapping**: Configured non-root `spring` user and cloud port binding (`PORT=8085`).
- **Docker Ignore**: Configured `.dockerignore` to avoid copying build artifacts and test files into the Docker context (`f1937bc`).

### 3.3 imChamikara (Maven CI/CD Pipeline)
*2 commits on 2026-09-25 (PR #8)*
- **GitHub Actions Workflow**: Created `.github/workflows/maven-ci.yml` (`de48f48`, `a855aca`).
- **MySQL CI Service**: Configured an automated MySQL 8.4 container service in the GitHub Actions runner on port `3307` for automated pull request checks.

### 3.4 DIKKUMBURA D.K.C.P. (PR Management)
*1 commit on 2026-09-25*
- Reviewed and merged Pull Request #8 (`Merge pull request #8 from ams-mit/feature/AMS-G4-32-maven-ci`).

---

## 4. Git Commit History & Timeline

```mermaid
gitGraph
   commit id: "315f4b6" message: "Initial Setup (Sajana)"
   commit id: "fe81037" message: "COMM-001 Facilities (Sajana)"
   branch feature/community-booking-creation
   checkout feature/community-booking-creation
   commit id: "8b82eab" message: "COMM-002 Booking Creation (Sajana)"
   checkout main
   merge feature/community-booking-creation id: "ac50adc"
   branch feature/community-booking-status-update
   checkout feature/community-booking-status-update
   commit id: "5ad11cd" message: "COMM-003 Status Update (Sajana)"
   checkout main
   merge feature/community-booking-status-update id: "9d2eb32"
   branch feature/community-visitor-management
   checkout feature/community-visitor-management
   commit id: "b10866b" message: "COMM-004/005 Visitors (Sajana)"
   checkout main
   merge feature/community-visitor-management id: "f5b985f"
   branch feature/community-announcements
   checkout feature/community-announcements
   commit id: "30c9db6" message: "COMM-006/007 Announcements (Sajana)"
   checkout main
   merge feature/community-announcements id: "9e10c90"
   branch feature/AMS-G4-32-maven-ci
   checkout feature/AMS-G4-32-maven-ci
   commit id: "a855aca" message: "Maven CI Pipeline (imChamikara)"
   checkout main
   merge feature/AMS-G4-32-maven-ci id: "8ace594"
   branch feature/AMS-G4-30-dockerfiles
   checkout feature/AMS-G4-30-dockerfiles
   commit id: "04f05f8" message: "Dockerfile (sanduDM)"
   checkout main
   branch feature/community-canonical-apis
   checkout feature/community-canonical-apis
   commit id: "7760bc3" message: "COMM-001 to 012 Canonical APIs (Kaveesha)"
   checkout main
   merge feature/community-canonical-apis id: "f5571bc"
   commit id: "51d915f" message: "Fix CI Context Test & Workflow (Kaveesha)"
   merge feature/AMS-G4-30-dockerfiles id: "5cfbae6"
   branch error-fix
   checkout error-fix
   commit id: "6c85094" message: "Aiven MySQL Cloud & Schema (Kaveesha)"
```

---

## 5. Summary Matrix: Feature Ownership

| Feature / Component | Author | Status | Current Location |
| :--- | :--- | :---: | :--- |
| Initial Spring Boot Setup | Sajana Jayawardhana | Completed | Root repo |
| Basic Facility & Booking Endpoints | Sajana Jayawardhana | Completed | Controller / Service |
| Maven GitHub Actions CI Pipeline | imChamikara | Completed | `.github/workflows/maven-ci.yml` |
| Multi-stage Dockerfile Setup | sanduDM | Completed | `Dockerfile` |
| **All 12 Canonical API Endpoints** | **Kaveesha Vimukthi** | Completed | Controllers, DTOs, Repos |
| **Notification Engine & Event Triggers**| **Kaveesha Vimukthi** | Completed | `NotificationService.java` |
| **Surefire / CI Build Failure Fix** | **Kaveesha Vimukthi** | Completed | Tests & CI Workflow |
| **Aiven Cloud MySQL Setup & Schema** | **Kaveesha Vimukthi** | Completed | `application-mysql.properties` |
| **Secrets & Push Protection (.env)** | **Kaveesha Vimukthi** | Completed | `.env`, `.env.example` |
| **React + TypeScript UI Application** | **Kaveesha Vimukthi** | Completed | `community-service-ui/` |
