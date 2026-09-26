# Community Service — Canonical API Specification & Implementation Registry

Based on the canonical system registry defined in [`project-a-canonical-complete-api-endpoint-registry.md`](file:///e:/Degree/IT/Sem%204/SA%20&%20PM/AMS-MIT/Community%20Service/project-a-canonical-complete-api-endpoint-registry.md), this document details all 12 APIs designated for **`community-service`**, their contract specifications, backend implementations, and frontend integrations.

---

## 1. Canonical Endpoint Matrix

| API ID | Method | Endpoint | API Type | Authentication | Required Role | Purpose | System Status |
|---|---|---|---|---|---|---|---|
| **COMM-001** | `GET` | `/api/v1/facilities` | USER | Gateway User JWT | Authenticated roles | List available facilities | Fully Implemented |
| **COMM-002** | `POST` | `/api/v1/facilities/reservations` | USER | Gateway User JWT | `RESIDENT`, `TENANT`, `OWNER` | Create facility reservation | Fully Implemented |
| **COMM-003** | `PATCH` | `/api/v1/facilities/reservations/{bookingId}/status` | USER | Gateway User JWT | `APARTMENT_MANAGER`, `SERVICE_STAFF`, `SYSTEM_ADMIN` | Approve / reject / cancel reservation | Fully Implemented |
| **COMM-004** | `POST` | `/api/v1/visitors` | USER | Gateway User JWT | `RESIDENT`, `TENANT`, `OWNER` | Register visitor | Fully Implemented |
| **COMM-005** | `PATCH` | `/api/v1/visitors/{visitorId}/check-in` | USER | Gateway User JWT | `SECURITY_OFFICER` | Check visitor in | Fully Implemented |
| **COMM-006** | `POST` | `/api/v1/announcements` | USER | Gateway User JWT | `SYSTEM_ADMIN`, `APARTMENT_MANAGER` | Publish announcement | Fully Implemented |
| **COMM-007** | `GET` | `/api/v1/announcements` | USER | Gateway User JWT | Authenticated roles | List visible announcements | Fully Implemented |
| **COMM-008** | `POST` | `/api/v1/notifications` | INTERNAL | Gateway Service JWT | Authorized services | Create workflow notification | Fully Implemented |
| **COMM-009** | `GET` | `/api/v1/notifications` | USER | Gateway User JWT | Authenticated roles | List current user's notifications | Fully Implemented |
| **COMM-010** | `PATCH` | `/api/v1/notifications/{notificationId}/read` | USER | Gateway User JWT | Authenticated user | Mark notification as read | Fully Implemented |
| **COMM-011** | `GET` | `/actuator/health` | HEALTH | None | None | Service health | Fully Implemented |
| **COMM-012** | `GET` | `/actuator/info` | HEALTH | None | None | Service metadata | Fully Implemented |

---

## 2. Endpoint Specifications & Contracts

### COMM-001: List Available Facilities
- **Method**: `GET`
- **Route**: `/api/v1/facilities`
- **Controller**: [`FacilityController.java`](file:///e:/Degree/IT/Sem%204/SA%20&%20PM/AMS-MIT/Community%20Service/src/main/java/lk/ac/kln/apartment/community_service/controller/FacilityController.java)
- **Response**: `200 OK` — `List<FacilityResponse>`

### COMM-002: Create Facility Reservation
- **Method**: `POST`
- **Route**: `/api/v1/facilities/reservations`
- **Controller**: [`BookingController.java`](file:///e:/Degree/IT/Sem%204/SA%20&%20PM/AMS-MIT/Community%20Service/src/main/java/lk/ac/kln/apartment/community_service/controller/BookingController.java)
- **Request Body**:
  ```json
  {
    "facilityId": 1,
    "startTime": "2026-09-30T10:00:00",
    "endTime": "2026-09-30T12:00:00",
    "requesterId": "resident-001",
    "requesterRole": "RESIDENT",
    "requesterName": "John Doe",
    "unitId": "U-101",
    "purpose": "Gym training session",
    "attendeeCount": 2
  }
  ```
- **Response**: `201 CREATED` — `BookingResponse`

### COMM-003: Approve / Reject / Cancel Facility Reservation
- **Method**: `PATCH`
- **Route**: `/api/v1/facilities/reservations/{bookingId}/status`
- **Controller**: [`BookingController.java`](file:///e:/Degree/IT/Sem%204/SA%20&%20PM/AMS-MIT/Community%20Service/src/main/java/lk/ac/kln/apartment/community_service/controller/BookingController.java)
- **Request Body**:
  ```json
  {
    "status": "APPROVED", // APPROVED, REJECTED, or CANCELLED
    "rejectionReason": null
  }
  ```
- **Response**: `200 OK` — `BookingResponse`
- **Workflow Automation**: When approved, conflicting pending bookings are auto-rejected, and an automatic workflow notification is emitted to the requester.

### COMM-004: Register Visitor
- **Method**: `POST`
- **Route**: `/api/v1/visitors`
- **Controller**: [`VisitorController.java`](file:///e:/Degree/IT/Sem%204/SA%20&%20PM/AMS-MIT/Community%20Service/src/main/java/lk/ac/kln/apartment/community_service/controller/VisitorController.java)
- **Request Body**:
  ```json
  {
    "visitorName": "Michael Scott",
    "residentId": "resident-001",
    "unitId": "U-101",
    "purpose": "Friend visit",
    "visitDate": "2026-09-30",
    "visitorPhone": "+94771234567",
    "vehicleNumber": "WP-CAB-1234"
  }
  ```
- **Response**: `201 CREATED` — `VisitorResponse` (with generated unique `passCode`)

### COMM-005: Check Visitor In
- **Method**: `PATCH`
- **Route**: `/api/v1/visitors/{visitorId}/check-in`
- **Controller**: [`VisitorController.java`](file:///e:/Degree/IT/Sem%204/SA%20&%20PM/AMS-MIT/Community%20Service/src/main/java/lk/ac/kln/apartment/community_service/controller/VisitorController.java)
- **Response**: `200 OK` — `VisitorResponse`
- **Workflow Automation**: Dispatches an instant security arrival notification to the resident (`residentId`).

### COMM-006: Publish Announcement
- **Method**: `POST`
- **Route**: `/api/v1/announcements`
- **Controller**: [`AnnouncementController.java`](file:///e:/Degree/IT/Sem%204/SA%20&%20PM/AMS-MIT/Community%20Service/src/main/java/lk/ac/kln/apartment/community_service/controller/AnnouncementController.java)
- **Request Body**:
  ```json
  {
    "title": "Quarterly Fire Alarm Inspection",
    "content": "Testing sirens and fire doors this Saturday at 10 AM.",
    "targetRole": "ALL",
    "publishedBy": "Central Operations",
    "category": "MAINTENANCE",
    "priority": "HIGH"
  }
  ```
- **Response**: `201 CREATED` — `AnnouncementResponse`
- **Workflow Automation**: Automatically creates and broadcasts a community bulletin notification to target roles.

### COMM-007: List Visible Announcements
- **Method**: `GET`
- **Route**: `/api/v1/announcements?role=RESIDENT`
- **Controller**: [`AnnouncementController.java`](file:///e:/Degree/IT/Sem%204/SA%20&%20PM/AMS-MIT/Community%20Service/src/main/java/lk/ac/kln/apartment/community_service/controller/AnnouncementController.java)
- **Response**: `200 OK` — `List<AnnouncementResponse>`

### COMM-008: Create Workflow Notification (Internal)
- **Method**: `POST`
- **Route**: `/api/v1/notifications`
- **Type**: `INTERNAL` (used by operations-service, billing-service, lease-service, or internal workflows)
- **Controller**: [`NotificationController.java`](file:///e:/Degree/IT/Sem%204/SA%20&%20PM/AMS-MIT/Community%20Service/src/main/java/lk/ac/kln/apartment/community_service/controller/NotificationController.java)
- **Request Body**:
  ```json
  {
    "recipientId": "resident-001",
    "recipientRole": "RESIDENT",
    "title": "Water Outage Complete",
    "summary": "Pressure valves repaired.",
    "message": "Water pressure has been normalized across all Tower A floors.",
    "type": "maintenance",
    "category": "MAINTENANCE",
    "priority": "normal",
    "issuedBy": "Operations Division",
    "affectedArea": "Tower A",
    "actionRoute": "/facilities",
    "actionLabel": "View Facilities"
  }
  ```
- **Response**: `201 CREATED` — `NotificationResponse`

### COMM-009: List Current User's Notifications
- **Method**: `GET`
- **Route**: `/api/v1/notifications?recipientId={recipientId}&role={role}&unreadOnly={true|false}`
- **Header Alternative**: `X-User-Id`, `X-User-Role`
- **Controller**: [`NotificationController.java`](file:///e:/Degree/IT/Sem%204/SA%20&%20PM/AMS-MIT/Community%20Service/src/main/java/lk/ac/kln/apartment/community_service/controller/NotificationController.java)
- **Response**: `200 OK` — `List<NotificationResponse>` (sorted newest first)

### COMM-010: Mark Notification as Read
- **Method**: `PATCH`
- **Route**: `/api/v1/notifications/{notificationId}/read`
- **Controller**: [`NotificationController.java`](file:///e:/Degree/IT/Sem%204/SA%20&%20PM/AMS-MIT/Community%20Service/src/main/java/lk/ac/kln/apartment/community_service/controller/NotificationController.java)
- **Response**: `200 OK` — `NotificationResponse` (`isRead: true`, `readAt: timestamp`)

### COMM-011: Service Health
- **Method**: `GET`
- **Route**: `/actuator/health`
- **Provider**: Spring Boot Actuator
- **Response**: `200 OK` — `{"status": "UP", "components": { ... }}`

### COMM-012: Service Metadata
- **Method**: `GET`
- **Route**: `/actuator/info`
- **Provider**: Spring Boot Actuator
- **Response**: `200 OK` — `{"app": {"name": "community-service", "version": "0.0.1-SNAPSHOT"}}`

---

## 3. Verification & Testing

- **Backend Unit & Integration Tests**: 13 tests passing (`BookingServiceTest`, `NotificationServiceTest`, `NotificationControllerTest`, `CommunityServiceApplicationTests`).
- **Frontend Build**: `npm run build` completed with 0 errors.
- **Postman Collection**: Updated at [`postman/Community Service API.postman_collection.json`](file:///e:/Degree/IT/Sem%204/SA%20&%20PM/AMS-MIT/Community%20Service/postman/Community%20Service%20API.postman_collection.json) covering all 12 endpoints.
