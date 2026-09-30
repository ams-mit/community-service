# Project A — Community Service

**Service:** `community-service`  
**Group:** Group 4 — Apartment Operations and Community  
**Project:** Apartment Management System (AMS)  
**Package:** `kln.ams.community`  
**Database:** `community_db`  
**Canonical Base Path:** `/api/v1`  
**Version:** `1.0.0`

---

## 1. Service Overview

The `community-service` is the sole authoritative owner of the **Community** domain within Project A, strictly responsible for:
- **Visitors:** Visitor request registration, management approval/rejection, check-in, check-out, visit history, and access auditing.
- **Announcements:** Announcement drafting, role/unit targeting, scheduled publishing, cancellation, audience preview, and per-user read state tracking.
- **Notifications:** Authoritative persistence and delivery of in-application notifications for residents, cross-service notification ingestion from registered Project A services (`operations-service`, `billing-payment-service`, `utility-charge-service`, `lease-occupancy-service`), and recipient-isolated notification centers.

Per **PROJECT-A-CONTRACT-DECISIONS.md**, Group 4 ownership is divided as follows:
- `operations-service`: Maintenance requests, work orders, assignments, facilities, and facility bookings.
- `community-service`: Visitors, announcements, and notifications.

Facilities and Bookings are non-responsibilities of this service.

---

## 2. Technology Stack

- **Java:** OpenJDK 21 (Temurin)
- **Framework:** Spring Boot 4.1.1
- **Database:** MySQL 8.0 (Production / Azure / Aiven Cloud) / Embedded H2 (Local Dev / Test)
- **Persistence:** Spring Data JPA / Hibernate
- **Security:** RS256 JWT (Gateway-signed tokens, role and service authorization)
- **API Spec:** OpenAPI 3 / Swagger (`springdoc-openapi-starter-webmvc-ui`)
- **Build Tool:** Maven Wrapper (`mvnw` / `mvnw.cmd`)
- **Containerization:** Multi-stage Dockerfile (`eclipse-temurin:21-jre-alpine`)

---

## 3. Architecture & Repository Structure

```text
community-service/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── README.md
├── .env.example
├── .gitignore
├── postman/
│   ├── Community Service API.postman_collection.json
│   ├── Community_Service_Environment.postman_environment.json
│   └── Community_Service_Azure_Environment.postman_environment.json
├── src/
│   ├── main/
│   │   ├── java/kln/ams/community/
│   │   │   ├── CommunityServiceApplication.java
│   │   │   ├── config/
│   │   │   │   ├── DataSeeder.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AnnouncementController.java
│   │   │   │   ├── HomeController.java
│   │   │   │   ├── InternalNotificationController.java
│   │   │   │   ├── NotificationController.java
│   │   │   │   └── VisitorController.java
│   │   │   ├── dto/
│   │   │   │   ├── ApiErrorResponse.java
│   │   │   │   ├── ApiResponse.java
│   │   │   │   ├── PaginationDto.java
│   │   │   │   ├── AnnouncementCreateRequest.java
│   │   │   │   ├── AnnouncementUpdateRequest.java
│   │   │   │   ├── AnnouncementCancelRequest.java
│   │   │   │   ├── AnnouncementResponse.java
│   │   │   │   ├── AnnouncementReadResponse.java
│   │   │   │   ├── AudiencePreviewResponse.java
│   │   │   │   ├── InternalNotificationRequest.java
│   │   │   │   ├── InternalResidentNotificationRequest.java
│   │   │   │   ├── NotificationReadAllResponse.java
│   │   │   │   ├── NotificationResponse.java
│   │   │   │   ├── NotificationStatusResponse.java
│   │   │   │   ├── NotificationSummaryResponse.java
│   │   │   │   ├── VisitorRequest.java
│   │   │   │   ├── VisitorResponse.java
│   │   │   │   ├── VisitorStatusRequest.java
│   │   │   │   └── VisitorUpdateRequest.java
│   │   │   ├── entity/
│   │   │   │   ├── Announcement.java
│   │   │   │   ├── AnnouncementAudience.java
│   │   │   │   ├── AnnouncementRead.java
│   │   │   │   ├── AnnouncementStatus.java
│   │   │   │   ├── AudienceType.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── NotificationStatus.java
│   │   │   │   ├── Visitor.java
│   │   │   │   ├── VisitorHistory.java
│   │   │   │   └── VisitorStatus.java
│   │   │   ├── exception/
│   │   │   │   ├── BusinessRuleViolationException.java
│   │   │   │   ├── DependencyUnavailableException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── ForbiddenException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── InvalidStatusTransitionException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── UnauthorizedException.java
│   │   │   ├── repository/
│   │   │   │   ├── AnnouncementAudienceRepository.java
│   │   │   │   ├── AnnouncementReadRepository.java
│   │   │   │   ├── AnnouncementRepository.java
│   │   │   │   ├── NotificationRepository.java
│   │   │   │   ├── VisitorHistoryRepository.java
│   │   │   │   └── VisitorRepository.java
│   │   │   ├── security/
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── JwtAuthPrincipal.java
│   │   │   │   ├── JwtService.java
│   │   │   │   ├── RoleConstants.java
│   │   │   │   └── SecurityUtils.java
│   │   │   └── service/
│   │   │       ├── AnnouncementService.java
│   │   │       ├── NotificationService.java
│   │   │       └── VisitorService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-mysql.properties
│   │       └── application-postgres.properties
│   └── test/
│       ├── java/kln/ams/community/
│       │   ├── CommunityServiceApplicationTests.java
│       │   ├── controller/
│       │   │   ├── AnnouncementControllerTest.java
│       │   │   ├── InternalNotificationControllerTest.java
│       │   │   ├── NotificationControllerTest.java
│       │   │   └── VisitorControllerTest.java
│       │   └── service/
│       │       ├── AnnouncementServiceTest.java
│       │       ├── NotificationServiceTest.java
│       │       └── VisitorServiceTest.java
│       └── resources/
│           ├── application.properties
│           └── application-test.properties
```

---

## 4. Canonical API Endpoint Inventory (28 Endpoints)

### 4.1 Visitor APIs (8 Endpoints)

| ID | Method | Endpoint | Description | Roles |
|---|---|---|---|---|
| COMM-VIS-001 | `POST` | `/api/v1/visitors` | Create visitor request | `TENANT_RESIDENT`, `OWNER`, `APARTMENT_MANAGER`, `SECURITY_OFFICER` |
| COMM-VIS-002 | `GET` | `/api/v1/visitors` | List visitors with filter & pagination | `SYSTEM_ADMINISTRATOR`, `APARTMENT_MANAGER`, `SECURITY_OFFICER`, `TENANT_RESIDENT`, `OWNER` |
| COMM-VIS-003 | `GET` | `/api/v1/visitors/{visitorId}` | Get visitor details | Relationship / Staff scoped |
| COMM-VIS-004 | `PATCH` | `/api/v1/visitors/{visitorId}` | Update visitor request | Creator / Staff |
| COMM-VIS-005 | `POST` | `/api/v1/visitors/{visitorId}/approve` | Approve visitor | `SECURITY_OFFICER`, `APARTMENT_MANAGER` |
| COMM-VIS-006 | `POST` | `/api/v1/visitors/{visitorId}/reject` | Reject visitor with reason | `SECURITY_OFFICER`, `APARTMENT_MANAGER` |
| COMM-VIS-007 | `POST` | `/api/v1/visitors/{visitorId}/check-in` | Check-in approved visitor | `SECURITY_OFFICER`, `APARTMENT_MANAGER` |
| COMM-VIS-008 | `POST` | `/api/v1/visitors/{visitorId}/check-out` | Check-out visitor | `SECURITY_OFFICER`, `APARTMENT_MANAGER` |

### 4.2 Announcement APIs (8 Endpoints)

| ID | Method | Endpoint | Description | Roles |
|---|---|---|---|---|
| COMM-ANN-001 | `POST` | `/api/v1/announcements` | Create announcement (DRAFT) | `APARTMENT_MANAGER`, `SYSTEM_ADMINISTRATOR` |
| COMM-ANN-002 | `GET` | `/api/v1/announcements` | List announcements (Audience filtered) | Authenticated users |
| COMM-ANN-003 | `GET` | `/api/v1/announcements/{announcementId}` | Get announcement | Authenticated users |
| COMM-ANN-004 | `PATCH` | `/api/v1/announcements/{announcementId}` | Update announcement | `APARTMENT_MANAGER`, `SYSTEM_ADMINISTRATOR` |
| COMM-ANN-005 | `POST` | `/api/v1/announcements/{announcementId}/publish` | Publish announcement | `APARTMENT_MANAGER`, `SYSTEM_ADMINISTRATOR` |
| COMM-ANN-006 | `POST` | `/api/v1/announcements/{announcementId}/cancel` | Cancel announcement | `APARTMENT_MANAGER`, `SYSTEM_ADMINISTRATOR` |
| COMM-ANN-007 | `POST` | `/api/v1/announcements/{announcementId}/read` | Mark announcement as read | `TENANT_RESIDENT`, `OWNER`, `APARTMENT_MANAGER`, etc. |
| COMM-ANN-008 | `GET` | `/api/v1/announcements/{announcementId}/audience-preview` | Preview target audience size | `APARTMENT_MANAGER`, `SYSTEM_ADMINISTRATOR` |

### 4.3 Notification APIs (7 Endpoints)

| ID | Method | Endpoint | Description | Roles |
|---|---|---|---|---|
| COMM-NOT-001 | `GET` | `/api/v1/notifications` | List user's notifications | Current authenticated user |
| COMM-NOT-002 | `GET` | `/api/v1/notifications/{notificationId}` | Get notification by ID | Current recipient |
| COMM-NOT-003 | `POST` | `/api/v1/notifications/{notificationId}/read` | Mark notification as read | Current recipient |
| COMM-NOT-004 | `POST` | `/api/v1/notifications/read-all` | Mark all notifications read | Current recipient |
| COMM-NOT-005 | `GET` | `/api/v1/notifications/summary` | Get unread/total count badge | Current recipient |
| COMM-NOT-006 | `GET` | `/api/v1/notifications/history` | Notification history | Current recipient |
| COMM-NOT-007 | `POST` | `/api/v1/notifications/{notificationId}/archive` | Archive notification | Current recipient |

### 4.4 Internal Service APIs (3 Endpoints)

| ID | Method | Endpoint | Description | Allowed Callers |
|---|---|---|---|---|
| COMM-INT-001 | `POST` | `/api/v1/internal/notifications` | Ingest notification | Registered backend services |
| COMM-INT-002 | `GET` | `/api/v1/internal/notifications/{notificationId}/status` | Get delivery/read status | Registered backend services |
| COMM-INT-003 | `POST` | `/api/v1/internal/notifications/resident` | Ingest resident event notification | Registered backend services |

### 4.5 Operational Endpoints (2 Endpoints)

| ID | Method | Endpoint | Description |
|---|---|---|---|
| COMM-OPS-001 | `GET` | `/actuator/health` | Health checks (Liveness/Readiness/DB) |
| COMM-OPS-002 | `GET` | `/actuator/info` | Application metadata |

---

## 5. Standard Envelopes

### Success Envelope
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "pagination": {
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrevious": false
  },
  "timestamp": "2026-09-30T10:00:00Z",
  "requestId": "7f83a9b2-0000-0000-0000-000000000000"
}
```

### Error Envelope
```json
{
  "success": false,
  "message": "Validation failed",
  "error": {
    "code": "VALIDATION_ERROR",
    "details": [
      { "field": "visitorName", "message": "visitorName is required" }
    ]
  },
  "timestamp": "2026-09-30T10:00:00Z",
  "requestId": "7f83a9b2-0000-0000-0000-000000000000"
}
```

---

## 6. Security & Roles

Project A canonical roles:
- `SYSTEM_ADMINISTRATOR`
- `APARTMENT_MANAGER`
- `SECURITY_OFFICER`
- `OWNER`
- `TENANT_RESIDENT`
- `FINANCE_OFFICER`
- `MAINTENANCE_COORDINATOR`
- `TECHNICIAN`
- `SERVICE_STAFF`

Tokens are signed using RS256 by the Project A API Gateway. Internal service-to-service calls use Gateway-issued Service assertions.

---

## 7. Running Locally

### Prerequisites
- JDK 21+
- Maven 3.8+ (or use `./mvnw`)

### Build & Run
```bash
# Compile and test
./mvnw clean test

# Run with local H2 database
./mvnw spring-boot:run

# Run with MySQL Cloud
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

API Documentation will be available at:
- Swagger UI: `http://localhost:8085/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8085/v3/api-docs`
- Health check: `http://localhost:8085/actuator/health`
