# Project A — Community Service

**Service:** `community-service`  
**Project:** Apartment Management System  
**Group:** Group 4 — Apartment Operations and Community  
**API version:** `v1`  
**Canonical base path:** `/api/v1`  
**Repository:** `community-service`  
**Java package:** `kln.ams.community`  
**Database:** `community_db`

---

# 1. Purpose

This document defines the canonical target contract for `community-service` in Project A.

The service owns the **community domain** inside Group 4:

```text
Visitor
Announcement
Notification
```

`operations-service` is the other Group 4 service. It owns:

```text
MaintenanceRequest
WorkOrder
Assignment
Facility
Booking
```

The separation is intentional:

```text
operations-service
    ├── maintenance
    ├── work orders
    ├── assignments
    ├── facilities
    └── bookings

community-service
    ├── visitors
    ├── announcements
    └── notifications
```

There must be exactly one authoritative owner for each of these domain resources.

> **Canonical implementation rule:** this document describes the target Project A contract. The existing repository is not the source of truth. Inspect the complete repository and remove obsolete, duplicate, conflicting, or unwanted APIs and implementation. Create or modify implementation until the repository is synchronized with this contract.

---

# 2. Source Basis

The Apartment Management System case identifies Group 4 as owning:

```text
operations-service
community-service
```

and the frontend areas:

```text
/maintenance
/work-orders
/complaints
/facilities
/bookings
/visitors
/announcements
/notifications
```

The indicative Group 4 entities are:

```text
MaintenanceRequest
WorkOrder
Assignment
Facility
Booking
Visitor
Announcement
Notification
```

fileciteturn67file0

The case requires community-related functionality including:

- recording visitor requests or visits with resident/unit reference, date, purpose, status, and basic check-in information;
- publishing role-targeted announcements;
- maintaining read or visibility status where feasible;
- providing in-application notifications when important workflow events occur. fileciteturn67file2

Group 4's minimum user stories include:

- authorized staff can manage visitor records;
- authorized staff can publish announcements;
- users receive selected workflow notifications.

The required access is role-restricted. fileciteturn67file0

The project scope explicitly states that real SMS/email delivery is out of scope; in-application or simulated notifications are sufficient. fileciteturn67file7

---

# 3. Source Limitations

The assignment does **not** prescribe:

- exact Community API endpoint inventory;
- exact endpoint IDs;
- exact URI shapes;
- exact visitor fields beyond the described information;
- exact announcement fields;
- exact notification fields;
- exact audience-rule model;
- exact notification delivery-state model;
- exact visitor status enum;
- exact announcement lifecycle;
- exact notification read-state implementation;
- exact internal Community APIs.

The endpoint inventory and detailed schemas below are therefore **Project A contract decisions** created to make implementation and cross-service integration deterministic.

They must be synchronized with the project-wide API registry.

---

# 4. Service Boundary

## 4.1 Community owns

```text
Visitor
Announcement
Notification
```

## 4.2 Operations owns

```text
MaintenanceRequest
WorkOrder
Assignment
Facility
Booking
```

Operations may **produce notification events**, but Community owns the Notification resource.

## 4.3 Other service ownership

Identity:

```text
User
Role
Permission
authentication
account status
```

Resident:

```text
Resident
Owner
Tenant
Staff
user-domain relationships
```

Property:

```text
Building
Floor
Unit
UnitType
Ownership
unit status
```

Lease/Occupancy:

```text
Lease
Occupancy
Occupant
occupancy status/history
```

Billing:

```text
ChargeRule
Invoice
InvoiceLine
Payment
Receipt
Adjustment
balance
arrears
```

Utility:

```text
UtilityCharge
```

No direct database access is permitted across these boundaries.

---

# 5. Service Identity

| Item | Canonical value |
|---|---|
| Service | `community-service` |
| Domain | Visitors, announcements, notifications |
| Group | Group 4 |
| Package | `kln.ams.community` |
| Repository | `community-service` |
| Database | `community_db` |
| API base | `/api/v1` |
| Internal API base | `/api/v1/internal` |
| Java | 21 |
| Framework | Spring Boot 4.1.1 |
| Build | Maven |
| Database | MySQL |
| API | REST + JSON |
| Documentation | OpenAPI / Swagger |
| Authentication | JWT Bearer |
| Gateway | Project A API Gateway |

---

# 6. Responsibilities

`community-service` is responsible for:

1. Visitor request creation.
2. Visitor approval/rejection.
3. Visitor check-in.
4. Visitor check-out.
5. Visitor history.
6. Resident/unit visitor relationship.
7. Visitor access control.
8. Announcement creation.
9. Announcement publishing.
10. Announcement targeting.
11. Announcement visibility/read state where implemented.
12. Announcement lifecycle.
13. In-application notifications.
14. Notification targeting.
15. Notification read/unread state.
16. Notification delivery state.
17. Notification history.
18. Internal notification ingestion from other services.
19. Role-based authorization.
20. Apartment/unit relationship validation where required.
21. Cross-service integration with Group 1 and Group 2.
22. Notification consumption/production integration with Operations and other services.
23. Audit-friendly community workflow metadata.

---

# 7. Explicit Non-Responsibilities

This service must not own:

```text
User
Role
Permission
Resident
Owner
Tenant
Staff
Building
Floor
Unit
Ownership
Lease
Occupancy
ChargeRule
Invoice
InvoiceLine
Payment
Receipt
Adjustment
UtilityCharge
MaintenanceRequest
WorkOrder
Assignment
Facility
Booking
```

It must also not own:

```text
real SMS delivery
real email delivery
real gate hardware
biometric visitor verification
smart locks
physical access-control devices
external identity provider
```

The assignment explicitly places real SMS/email delivery outside scope. In-application or simulated notifications are sufficient. fileciteturn67file7

---

# 8. Core Domain Model

## 8.1 Visitor

Conceptual fields:

```text
id
residentUserId
unitId
visitorName
visitorContact
visitDate
expectedArrival
expectedDeparture
purpose
status
checkInAt
checkOutAt
notes
createdByUserId
createdAt
updatedAt
```

The assignment specifically requires visitor records to retain a resident/unit reference, date, purpose, status, and basic check-in information. fileciteturn67file2

## 8.2 Announcement

Conceptual fields:

```text
id
title
content
category
priority
status
audienceType
scheduledAt
publishedAt
expiresAt
createdByUserId
createdAt
updatedAt
```

Audience configuration may additionally be represented through a separate local rule structure.

## 8.3 Notification

Conceptual fields:

```text
id
recipientUserId
type
title
message
sourceService
sourceEntityType
sourceEntityId
priority
status
readAt
createdAt
expiresAt
```

Notification ownership is exclusively Community.

---

# 9. Visitor Status

Recommended Project A status model:

```text
PENDING
APPROVED
REJECTED
CHECKED_IN
CHECKED_OUT
CANCELLED
EXPIRED
```

The exact enum is a Project A contract decision.

Typical lifecycle:

```text
PENDING
   |
   +----> APPROVED
   |
   +----> REJECTED
   |
   +----> CANCELLED

APPROVED
   |
   +----> CHECKED_IN
   |
   +----> CANCELLED
   |
   +----> EXPIRED

CHECKED_IN
   |
   +----> CHECKED_OUT
```

A rejected visitor cannot be checked in.

A cancelled visitor cannot be checked in.

A checked-out visit must not return to `CHECKED_IN` without an explicitly defined correction workflow.

---

# 10. Announcement Status

Recommended:

```text
DRAFT
SCHEDULED
PUBLISHED
EXPIRED
CANCELLED
```

Typical lifecycle:

```text
DRAFT
   |
   +----> SCHEDULED
   |
   +----> PUBLISHED
   |
   +----> CANCELLED

SCHEDULED
   |
   +----> PUBLISHED
   |
   +----> CANCELLED

PUBLISHED
   |
   +----> EXPIRED
   |
   +----> CANCELLED
```

The implementation must reject invalid transitions.

---

# 11. Notification Status

Recommended:

```text
UNREAD
READ
ARCHIVED
```

Delivery state may be represented separately:

```text
PENDING
DELIVERED
FAILED
```

The service must not confuse:

```text
notification delivery
```

with:

```text
user read state
```

For example:

```text
delivery = DELIVERED
readState = UNREAD
```

is valid.

Because notifications are in-application, "delivery" means successful persistence/availability to the intended application recipient, not SMS/email delivery.

---

# 12. Canonical API Inventory

The canonical Community contract contains:

```text
Visitor APIs:          8
Announcement APIs:     8
Notification APIs:     7
Internal APIs:         3
Operational APIs:      2
```

Total:

```text
28 endpoints
```

The exact endpoint inventory is a Project A contract decision.

---

# 13. Visitor APIs

## COMM-VIS-001 — Create Visitor Request

```http
POST /api/v1/visitors
```

**Roles:**

```text
TENANT_RESIDENT
OWNER
APARTMENT_MANAGER
SECURITY_OFFICER
```

### Purpose

Create a visitor request associated with a valid resident/unit relationship.

### Request

```json
{
  "unitId": "uuid",
  "visitorName": "Kasun Perera",
  "visitorContact": "0771234567",
  "visitDate": "2026-10-10",
  "expectedArrival": "18:00",
  "expectedDeparture": "20:00",
  "purpose": "Family visit",
  "notes": "Visitor will arrive by car"
}
```

### Rules

- requester must be authenticated;
- requester must have a valid relationship to the referenced unit unless an authorized management/security role creates the record;
- unit must exist;
- visitor name is required;
- visit date is required;
- purpose is required;
- expected departure must be later than expected arrival when both are provided;
- visitor contact is optional only when the project contract allows it.

### Success

```http
201 Created
```

### Default status

```text
PENDING
```

### Errors

```text
400 VALIDATION_ERROR
401 UNAUTHORIZED
403 FORBIDDEN
404 UNIT_NOT_FOUND
409 DUPLICATE_VISITOR_REQUEST
503 DEPENDENCY_UNAVAILABLE
```

---

# 14. List Visitors

## COMM-VIS-002

```http
GET /api/v1/visitors
```

### Filters

```text
unitId
residentUserId
status
visitDate
dateFrom
dateTo
visitorName
page
size
```

### Roles

```text
SYSTEM_ADMINISTRATOR
APARTMENT_MANAGER
SECURITY_OFFICER
TENANT_RESIDENT
OWNER
```

Residents and owners must receive only records within their authorized unit relationship.

Security and management users may have broader visibility.

---

# 15. Get Visitor

## COMM-VIS-003

```http
GET /api/v1/visitors/{visitorId}
```

Returns the visitor record and current visit state.

Access is relationship- or role-scoped.

---

# 16. Update Visitor Request

## COMM-VIS-004

```http
PATCH /api/v1/visitors/{visitorId}
```

### Editable fields

Depending on status:

```text
visitorName
visitorContact
visitDate
expectedArrival
expectedDeparture
purpose
notes
```

A checked-in or checked-out visit must not allow arbitrary edits.

---

# 17. Approve Visitor

## COMM-VIS-005

```http
POST /api/v1/visitors/{visitorId}/approve
```

**Roles:**

```text
SECURITY_OFFICER
APARTMENT_MANAGER
```

### Request

```json
{
  "reason": "Approved for scheduled resident visit"
}
```

Reason may be optional for approval but should be retained when supplied.

### Rules

- visitor must be `PENDING`;
- referenced unit must still be valid;
- resident/unit relationship must remain valid where required;
- approval must not be applied to a cancelled/rejected visitor.

---

# 18. Reject Visitor

## COMM-VIS-006

```http
POST /api/v1/visitors/{visitorId}/reject
```

**Roles:**

```text
SECURITY_OFFICER
APARTMENT_MANAGER
```

### Request

```json
{
  "reason": "Visitor information could not be validated"
}
```

Reason is mandatory.

A rejected visitor cannot be checked in.

---

# 19. Check In Visitor

## COMM-VIS-007

```http
POST /api/v1/visitors/{visitorId}/check-in
```

**Roles:**

```text
SECURITY_OFFICER
APARTMENT_MANAGER
```

### Rules

- visitor must be approved;
- visit must be valid for check-in according to the configured date/time rule;
- check-in timestamp is generated by the server;
- client must not provide an arbitrary authoritative `checkInAt`.

### Success

```http
200 OK
```

The service records:

```text
checkInAt
status = CHECKED_IN
```

---

# 20. Check Out Visitor

## COMM-VIS-008

```http
POST /api/v1/visitors/{visitorId}/check-out
```

**Roles:**

```text
SECURITY_OFFICER
APARTMENT_MANAGER
```

### Rules

- visitor must be `CHECKED_IN`;
- checkout timestamp is generated by the server;
- repeated checkout is rejected.

The service records:

```text
checkOutAt
status = CHECKED_OUT
```

---

# 21. Visitor Endpoint Summary

| ID | Method | Endpoint | Type |
|---|---|---|---|
| COMM-VIS-001 | POST | `/api/v1/visitors` | Public |
| COMM-VIS-002 | GET | `/api/v1/visitors` | Public |
| COMM-VIS-003 | GET | `/api/v1/visitors/{visitorId}` | Public |
| COMM-VIS-004 | PATCH | `/api/v1/visitors/{visitorId}` | Public |
| COMM-VIS-005 | POST | `/api/v1/visitors/{visitorId}/approve` | Public |
| COMM-VIS-006 | POST | `/api/v1/visitors/{visitorId}/reject` | Public |
| COMM-VIS-007 | POST | `/api/v1/visitors/{visitorId}/check-in` | Public |
| COMM-VIS-008 | POST | `/api/v1/visitors/{visitorId}/check-out` | Public |

---

# 22. Announcement APIs

## COMM-ANN-001 — Create Announcement

```http
POST /api/v1/announcements
```

**Roles:**

```text
APARTMENT_MANAGER
SYSTEM_ADMINISTRATOR
```

### Request

```json
{
  "title": "Water Supply Maintenance",
  "content": "Water supply will be interrupted from 10:00 to 12:00.",
  "category": "MAINTENANCE",
  "priority": "HIGH",
  "audienceType": "ALL_RESIDENTS",
  "scheduledAt": null,
  "expiresAt": "2026-10-15T23:59:59Z"
}
```

### Rules

- title is required;
- content is required;
- audience definition is required;
- expiry must be after publication/scheduled time when present;
- creator must have announcement-management permission.

### Default status

```text
DRAFT
```

---

# 23. List Announcements

## COMM-ANN-002

```http
GET /api/v1/announcements
```

### Filters

```text
status
category
priority
audienceType
publishedFrom
publishedTo
page
size
```

Residents receive only announcements visible to their audience.

Management users may receive broader results.

---

# 24. Get Announcement

## COMM-ANN-003

```http
GET /api/v1/announcements/{announcementId}
```

The service must evaluate audience visibility before returning an announcement to a normal resident/owner user.

---

# 25. Update Announcement

## COMM-ANN-004

```http
PATCH /api/v1/announcements/{announcementId}
```

**Roles:**

```text
APARTMENT_MANAGER
SYSTEM_ADMINISTRATOR
```

Possible editable fields:

```text
title
content
category
priority
audience
scheduledAt
expiresAt
```

Published announcements should have controlled edit rules.

A change to a published announcement must be auditable.

---

# 26. Publish Announcement

## COMM-ANN-005

```http
POST /api/v1/announcements/{announcementId}/publish
```

**Roles:**

```text
APARTMENT_MANAGER
SYSTEM_ADMINISTRATOR
```

### Rules

- announcement must be eligible for publication;
- audience must be configured;
- content must be valid;
- publication timestamp is server-controlled;
- published announcement becomes visible to eligible recipients.

### Success

```http
200 OK
```

---

# 27. Cancel Announcement

## COMM-ANN-006

```http
POST /api/v1/announcements/{announcementId}/cancel
```

**Roles:**

```text
APARTMENT_MANAGER
SYSTEM_ADMINISTRATOR
```

### Request

```json
{
  "reason": "Maintenance schedule changed"
}
```

Reason is required.

---

# 28. Announcement Read State

## COMM-ANN-007

```http
POST /api/v1/announcements/{announcementId}/read
```

**Roles:**

```text
TENANT_RESIDENT
OWNER
APARTMENT_MANAGER
```

### Purpose

Record that the current user has viewed/read an announcement.

The service derives the user from the authenticated token.

Do not accept arbitrary:

```text
userId
```

as the authoritative reader.

---

# 29. Announcement Audience Preview

## COMM-ANN-008

```http
GET /api/v1/announcements/{announcementId}/audience-preview
```

**Roles:**

```text
APARTMENT_MANAGER
SYSTEM_ADMINISTRATOR
```

### Purpose

Show management the effective audience before/after publication.

The response must not expose sensitive user information unnecessarily.

A summary is preferred:

```json
{
  "eligibleRecipientCount": 42,
  "audienceType": "ALL_RESIDENTS"
}
```

rather than returning full resident profiles.

---

# 30. Announcement Endpoint Summary

| ID | Method | Endpoint | Type |
|---|---|---|---|
| COMM-ANN-001 | POST | `/api/v1/announcements` | Public |
| COMM-ANN-002 | GET | `/api/v1/announcements` | Public |
| COMM-ANN-003 | GET | `/api/v1/announcements/{announcementId}` | Public |
| COMM-ANN-004 | PATCH | `/api/v1/announcements/{announcementId}` | Public |
| COMM-ANN-005 | POST | `/api/v1/announcements/{announcementId}/publish` | Public |
| COMM-ANN-006 | POST | `/api/v1/announcements/{announcementId}/cancel` | Public |
| COMM-ANN-007 | POST | `/api/v1/announcements/{announcementId}/read` | Public |
| COMM-ANN-008 | GET | `/api/v1/announcements/{announcementId}/audience-preview` | Public |

---

# 31. Notification APIs

Notification is a first-class Community resource.

Other services may request notification creation through the internal notification API, but they do not own Notification.

---

# 32. List My Notifications

## COMM-NOT-001

```http
GET /api/v1/notifications
```

**Roles:**

```text
authenticated users
```

### Filters

```text
status
type
priority
unreadOnly
dateFrom
dateTo
page
size
```

The recipient is derived from the authenticated user.

A normal user cannot request another user's notifications by simply changing:

```text
recipientUserId
```

in the query.

---

# 33. Get Notification

## COMM-NOT-002

```http
GET /api/v1/notifications/{notificationId}
```

A user may retrieve a notification only if:

```text
notification.recipientUserId == authenticatedUserId
```

or an authorized administrative access rule applies.

---

# 34. Mark Notification Read

## COMM-NOT-003

```http
POST /api/v1/notifications/{notificationId}/read
```

The authenticated user becomes the authoritative reader.

Server sets:

```text
status = READ
readAt = current server time
```

Repeated read requests should be idempotent where practical.

---

# 35. Mark All Notifications Read

## COMM-NOT-004

```http
POST /api/v1/notifications/read-all
```

Marks all currently unread notifications belonging to the authenticated user as read.

It must never mark another user's notifications.

---

# 36. Notification Summary

## COMM-NOT-005

```http
GET /api/v1/notifications/summary
```

Example:

```json
{
  "success": true,
  "message": "Notification summary retrieved",
  "data": {
    "unreadCount": 5,
    "totalCount": 18
  },
  "timestamp": "2026-10-01T10:00:00Z",
  "requestId": "uuid"
}
```

---

# 37. Notification History

## COMM-NOT-006

```http
GET /api/v1/notifications/history
```

For the current authenticated user.

Filters:

```text
type
sourceService
sourceEntityType
priority
status
dateFrom
dateTo
page
size
```

This endpoint supports a user's notification history without exposing other users' records.

---

# 38. Notification Archive

## COMM-NOT-007

```http
POST /api/v1/notifications/{notificationId}/archive
```

Marks the authenticated user's notification as archived.

Archive must not delete the authoritative notification record unless the project explicitly defines retention deletion.

---

# 39. Notification Endpoint Summary

| ID | Method | Endpoint | Type |
|---|---|---|---|
| COMM-NOT-001 | GET | `/api/v1/notifications` | Public |
| COMM-NOT-002 | GET | `/api/v1/notifications/{notificationId}` | Public |
| COMM-NOT-003 | POST | `/api/v1/notifications/{notificationId}/read` | Public |
| COMM-NOT-004 | POST | `/api/v1/notifications/read-all` | Public |
| COMM-NOT-005 | GET | `/api/v1/notifications/summary` | Public |
| COMM-NOT-006 | GET | `/api/v1/notifications/history` | Public |
| COMM-NOT-007 | POST | `/api/v1/notifications/{notificationId}/archive` | Public |

---

# 40. Internal APIs

The Community service exposes internal APIs for trusted Project A services.

These use:

```text
/api/v1/internal/...
```

and service-to-service authentication.

The internal API contract is:

```text
COMM-INT-001 POST /api/v1/internal/notifications
COMM-INT-002 GET  /api/v1/internal/notifications/{notificationId}/status
COMM-INT-003 POST /api/v1/internal/notifications/resident
```

These are Project A contract decisions and must be synchronized with the central API registry.

---

# 41. Internal Notification Creation

## COMM-INT-001

```http
POST /api/v1/internal/notifications
```

**Type:** INTERNAL

**Purpose:**

Allow another registered service to request creation of an in-application notification.

Example caller:

```text
operations-service
billing-payment-service
utility-charge-service
lease-occupancy-service
```

depending on documented workflow requirements.

### Request

```json
{
  "recipientUserId": "uuid",
  "type": "BOOKING_APPROVED",
  "title": "Booking approved",
  "message": "Your community hall booking has been approved.",
  "priority": "NORMAL",
  "sourceService": "operations-service",
  "sourceEntityType": "Booking",
  "sourceEntityId": "uuid"
}
```

### Rules

- caller must be a registered service;
- recipient user must be valid when recipient validation is required by the contract;
- source service must identify itself through the authenticated service identity;
- caller must not impersonate another service;
- notification type must be controlled;
- source entity identifiers must be syntactically valid;
- notification must be persisted before returning success.

### Success

```http
201 Created
```

---

# 42. Internal Notification Status

## COMM-INT-002

```http
GET /api/v1/internal/notifications/{notificationId}/status
```

### Purpose

Allow an authorized service to check whether a notification was successfully created and its current delivery/read state.

Example:

```json
{
  "success": true,
  "message": "Notification status retrieved",
  "data": {
    "notificationId": "uuid",
    "status": "UNREAD",
    "deliveryStatus": "DELIVERED",
    "readAt": null
  },
  "timestamp": "2026-10-01T10:00:00Z",
  "requestId": "uuid"
}
```

The response must not expose unrelated recipient profile information.

---

# 43. Internal Resident Notification

## COMM-INT-003

```http
POST /api/v1/internal/notifications/resident
```

### Purpose

Provide a convenient service-to-service operation for a workflow that needs to notify a resident/user about a domain event.

### Request

```json
{
  "residentUserId": "uuid",
  "type": "MAINTENANCE_REQUEST_UPDATED",
  "title": "Maintenance request updated",
  "message": "Your maintenance request is now in progress.",
  "priority": "NORMAL",
  "sourceService": "operations-service",
  "sourceEntityType": "MaintenanceRequest",
  "sourceEntityId": "uuid"
}
```

This endpoint remains a Community API. It must not cause the Community service to become the owner of the referenced domain entity.

---

# 44. Operational APIs

## COMM-OPS-001

```http
GET /actuator/health
```

Health should cover:

```text
application
database
```

## COMM-OPS-002

```http
GET /actuator/info
```

Must not expose:

```text
database credentials
JWT secrets
private keys
environment secrets
```

---

# 45. Complete Endpoint Summary

| ID | Method | Endpoint | Type |
|---|---|---|---|
| COMM-VIS-001 | POST | `/api/v1/visitors` | Public |
| COMM-VIS-002 | GET | `/api/v1/visitors` | Public |
| COMM-VIS-003 | GET | `/api/v1/visitors/{visitorId}` | Public |
| COMM-VIS-004 | PATCH | `/api/v1/visitors/{visitorId}` | Public |
| COMM-VIS-005 | POST | `/api/v1/visitors/{visitorId}/approve` | Public |
| COMM-VIS-006 | POST | `/api/v1/visitors/{visitorId}/reject` | Public |
| COMM-VIS-007 | POST | `/api/v1/visitors/{visitorId}/check-in` | Public |
| COMM-VIS-008 | POST | `/api/v1/visitors/{visitorId}/check-out` | Public |
| COMM-ANN-001 | POST | `/api/v1/announcements` | Public |
| COMM-ANN-002 | GET | `/api/v1/announcements` | Public |
| COMM-ANN-003 | GET | `/api/v1/announcements/{announcementId}` | Public |
| COMM-ANN-004 | PATCH | `/api/v1/announcements/{announcementId}` | Public |
| COMM-ANN-005 | POST | `/api/v1/announcements/{announcementId}/publish` | Public |
| COMM-ANN-006 | POST | `/api/v1/announcements/{announcementId}/cancel` | Public |
| COMM-ANN-007 | POST | `/api/v1/announcements/{announcementId}/read` | Public |
| COMM-ANN-008 | GET | `/api/v1/announcements/{announcementId}/audience-preview` | Public |
| COMM-NOT-001 | GET | `/api/v1/notifications` | Public |
| COMM-NOT-002 | GET | `/api/v1/notifications/{notificationId}` | Public |
| COMM-NOT-003 | POST | `/api/v1/notifications/{notificationId}/read` | Public |
| COMM-NOT-004 | POST | `/api/v1/notifications/read-all` | Public |
| COMM-NOT-005 | GET | `/api/v1/notifications/summary` | Public |
| COMM-NOT-006 | GET | `/api/v1/notifications/history` | Public |
| COMM-NOT-007 | POST | `/api/v1/notifications/{notificationId}/archive` | Public |
| COMM-INT-001 | POST | `/api/v1/internal/notifications` | Internal |
| COMM-INT-002 | GET | `/api/v1/internal/notifications/{notificationId}/status` | Internal |
| COMM-INT-003 | POST | `/api/v1/internal/notifications/resident` | Internal |
| COMM-OPS-001 | GET | `/actuator/health` | Operational |
| COMM-OPS-002 | GET | `/actuator/info` | Operational |

Total:

```text
28 endpoints
```

---

# 46. Visitor Business Rules

## Rule 1 — Valid resident/unit relationship

A visitor request must reference a valid apartment unit.

Where the requester is a resident/owner, the service must verify the user's relationship to that unit through the appropriate Group 1/Group 2 APIs.

Do not trust:

```text
unitId
```

from the frontend alone.

## Rule 2 — Valid unit

The unit must exist.

Use:

```text
property-unit-service
```

for authoritative unit validation.

## Rule 3 — Visitor lifecycle

Invalid visitor status transitions must be rejected.

## Rule 4 — Approval

Only authorized security/management roles may approve or reject visitor requests.

## Rule 5 — Check-in

Only approved visitors may be checked in.

## Rule 6 — Check-out

Only checked-in visitors may be checked out.

## Rule 7 — Server timestamps

Authoritative:

```text
checkInAt
checkOutAt
createdAt
updatedAt
```

must be generated by the server.

## Rule 8 — Privacy

Visitor information must be visible only to authorized users.

The case requires synthetic data and prohibits publishing real resident, contact, financial, or visitor information. fileciteturn67file7

---

# 47. Visitor Access Scope

A resident should be able to see:

```text
their visitor records
visitor records associated with their authorized unit
```

A security officer may see visitor records needed for security operations.

An apartment manager may see broader visitor information according to the role model.

A resident must not be able to query:

```text
another resident's visitor records
```

by changing:

```text
residentUserId
unitId
```

in a request.

Authorization must be evaluated from:

```text
JWT identity
+
role
+
verified apartment relationship
```

---

# 48. Visitor Duplicate Rule

The service should prevent accidental duplicate active visitor requests when the same:

```text
unit
visitor
visit date/time
```

combination already has an active record.

The exact duplicate definition is a Project A decision.

Do not reject legitimate repeated visits solely because the visitor name is the same.

---

# 49. Visitor Expiry

A pending/approved visitor may become:

```text
EXPIRED
```

after its valid visit window passes, according to the configured rule.

Expiry must not automatically imply:

```text
CHECKED_IN
```

or:

```text
CHECKED_OUT
```

It means the planned visit was no longer valid for entry.

---

# 50. Visitor History

Important visitor events should be auditable:

```text
VISITOR_CREATED
VISITOR_UPDATED
VISITOR_APPROVED
VISITOR_REJECTED
VISITOR_CHECKED_IN
VISITOR_CHECKED_OUT
VISITOR_CANCELLED
VISITOR_EXPIRED
```

History should include:

```text
event
previousStatus
newStatus
reason
changedBy
changedAt
requestId
```

---

# 51. Announcement Business Rules

## Rule 1 — Required content

Every announcement requires:

```text
title
content
audience
```

before publication.

## Rule 2 — Authorized publication

Only authorized management/system roles may publish announcements.

## Rule 3 — Audience enforcement

A published announcement must be visible only to its configured audience.

## Rule 4 — Role targeting

The project requires role-targeted announcements.

The audience model may support:

```text
ALL_RESIDENTS
ROLE
UNIT
CUSTOM
```

as a Project A decision.

## Rule 5 — Visibility

An announcement that is:

```text
DRAFT
CANCELLED
```

must not appear as a normal published announcement.

## Rule 6 — Expiry

Expired announcements may remain historically stored but should not appear in the active announcement feed unless an authorized management query requests historical data.

## Rule 7 — Read state

Read state belongs to the relationship:

```text
Announcement
    +
User
```

not to the global announcement itself.

Therefore:

```text
User A read = true
User B read = false
```

can both be valid.

---

# 52. Announcement Audience Model

The canonical audience concept may contain:

```text
audienceType
roleCodes
unitIds
```

For example:

```json
{
  "audienceType": "ROLE",
  "roleCodes": [
    "TENANT_RESIDENT",
    "OWNER"
  ]
}
```

or:

```json
{
  "audienceType": "UNIT",
  "unitIds": [
    "uuid-1",
    "uuid-2"
  ]
}
```

The exact schema is a Project A decision.

Do not store a giant denormalized copy of resident profiles in Community.

Use identity/resident/property APIs to validate audience references where required.

---

# 53. Announcement Targeting

The service may use these canonical audience categories:

```text
ALL_RESIDENTS
ROLE
UNIT
CUSTOM
```

`CUSTOM` must not become an excuse to copy complete user profiles into Community.

A custom audience should use identifiers/references.

---

# 54. Announcement Read Tracking

Recommended local structure:

```text
announcement_reads
```

Conceptual fields:

```text
announcementId
userId
readAt
```

Composite uniqueness:

```text
announcementId + userId
```

This prevents duplicate read records.

The service does not need to create a read row until the user actually reads the announcement.

---

# 55. Notification Business Rules

## Rule 1 — Notification owner

Community owns all notifications.

## Rule 2 — Source service

A notification records:

```text
sourceService
sourceEntityType
sourceEntityId
```

when it originates from another domain.

Example:

```text
sourceService = operations-service
sourceEntityType = Booking
sourceEntityId = <booking UUID>
```

## Rule 3 — Recipient

Notification recipient is a user identifier.

The authoritative User entity remains owned by Identity.

## Rule 4 — Read state

Read/unread state is per recipient.

## Rule 5 — Delivery state

For in-application notification:

```text
DELIVERED
```

means the notification has been persisted and made available to the application.

It does not mean SMS/email delivery.

## Rule 6 — No duplicate notification resource

Other services must not create their own:

```text
Notification
```

database table as an authoritative resource.

---

# 56. Notification Types

Recommended Project A controlled types include:

```text
MAINTENANCE_REQUEST_CREATED
MAINTENANCE_REQUEST_UPDATED
WORK_ORDER_ASSIGNED
WORK_ORDER_UPDATED
WORK_ORDER_COMPLETED
BOOKING_CREATED
BOOKING_APPROVED
BOOKING_REJECTED
BOOKING_CANCELLED
VISITOR_APPROVED
VISITOR_REJECTED
VISITOR_CHECKED_IN
ANNOUNCEMENT_PUBLISHED
PAYMENT_RECORDED
INVOICE_AVAILABLE
UTILITY_CHARGE_AVAILABLE
```

The exact notification-type registry is a Project A contract decision.

Services must not invent arbitrary incompatible type names.

---

# 57. Notification Priorities

Recommended:

```text
LOW
NORMAL
HIGH
URGENT
```

The exact set is a Project A decision.

---

# 58. Notification Creation Workflow

Example — Operations booking approval:

```text
Resident
   |
   v
Operations Service
   |
   | Booking approved
   v
POST /api/v1/internal/notifications
   |
   v
Community Service
   |
   | persist notification
   v
Notification available in application
```

Community does not need to know or own the booking business rules.

It only records the notification event supplied by the authoritative Operations service.

---

# 59. Maintenance Notification Workflow

Example:

```text
Maintenance Request
       |
       v
Operations Service
       |
       | request created/updated
       v
Community Internal Notification API
       |
       v
Notification
       |
       v
Resident notification list
```

The notification must reference:

```text
sourceService = operations-service
sourceEntityType = MaintenanceRequest
sourceEntityId = requestId
```

---

# 60. Billing Notification Workflow

If billing requires an in-application notification:

```text
Billing Payment Service
       |
       | invoice/payment event
       v
Community
       |
       v
Notification
```

Community must not calculate:

```text
invoice balance
payment status
arrears
```

It only delivers the supplied notification.

---

# 61. Utility Notification Workflow

If utility-charge-service needs to notify a resident:

```text
Utility Charge Service
       |
       v
Community internal notification API
       |
       v
Notification
```

No UtilityCharge data ownership is transferred to Community.

---

# 62. Identity Dependency

Community consumes Identity for:

```text
user validation
role/account validation
authenticated user identity
```

It does not maintain a second authoritative user table.

The JWT provides authenticated identity and roles.

Business relationship validation must still be performed through the appropriate domain service.

---

# 63. Resident Dependency

Community consumes Resident Management for:

```text
resident relationship
owner/tenant relationship
user-domain relationship
```

Examples:

```text
Is this user a resident?
Is this user associated with Unit U101?
Is this owner allowed to create a visitor for U101?
```

Do not query:

```text
resident_management_db
```

directly.

---

# 64. Property Dependency

Community may consume Property Unit Service for:

```text
unit existence
unit status
```

Visitor requests require a valid unit.

If the project decides that an inactive unit cannot receive visitor requests, Community must validate unit status before accepting the request.

---

# 65. Occupancy Dependency

Where current occupancy is required:

```text
lease-occupancy-service
```

is the authoritative provider.

Do not duplicate occupancy records.

Example:

```text
Visitor requester
    ↓
Resident relationship
    ↓
Current occupancy
    ↓
valid unit relationship
```

---

# 66. Operations Dependency

Operations may call Community when it needs to create a notification.

Example:

```text
operations-service
       |
       v
community-service
```

Community should not call Operations merely to display a notification.

The notification contains the source entity reference.

If a user selects a notification and needs details about the source:

```text
frontend
   |
   +----> Community notification
   |
   +----> source service endpoint
```

The source service remains authoritative.

---

# 67. Notification Provider/Consumer Principle

Provider:

```text
community-service
```

owns:

```text
Notification API
```

Consumers:

```text
operations-service
billing-payment-service
utility-charge-service
lease-occupancy-service
```

may reference it.

Consumers must not redefine:

```text
Notification response schema
```

inside their own contracts.

---

# 68. Service-to-Service Security

Internal APIs use the Project A service-to-service JWT architecture:

```text
Calling Service
      |
      | Service JWT
      v
API Gateway
      |
      | validate
      | create Gateway JWT
      v
Community Service
      |
      | verify Gateway JWT
      v
authorize service
```

Community trusts the Gateway-issued assertion.

Internal endpoints must reject:

```text
missing token
invalid token
expired token
wrong token type
unknown service
unauthorized service
invalid signature
```

---

# 69. JWT Requirements

Use the Project A security standard:

```text
RS256
```

Community backend must:

- verify Gateway signature;
- verify expiry;
- verify token type;
- authorize roles;
- authorize registered service callers;
- enforce relationship/business scope;
- never trust user identity from request body;
- never log JWTs;
- never commit private keys.

The project-wide architecture uses separate user/service token types and Gateway re-signing.

---

# 70. Authorization Matrix

| Capability | System Admin | Apartment Manager | Security Officer | Owner | Tenant/Resident |
|---|---:|---:|---:|---:|---:|
| Create visitor | Yes | Yes | Yes | Own | Own |
| View visitors | Yes | Yes | Yes | Own | Own |
| Update visitor | Yes | Yes | Yes | Own while editable | Own while editable |
| Approve visitor | Yes | Yes | Yes | No | No |
| Reject visitor | Yes | Yes | Yes | No | No |
| Check in visitor | Yes | Yes | Yes | No | No |
| Check out visitor | Yes | Yes | Yes | No | No |
| Create announcement | Yes | Yes | No | No | No |
| Update announcement | Yes | Yes | No | No | No |
| Publish announcement | Yes | Yes | No | No | No |
| Cancel announcement | Yes | Yes | No | No | No |
| Read announcement | Yes | Yes | Yes | Yes | Yes |
| View own notifications | Yes | Yes | Yes | Yes | Yes |
| Mark own notification read | Yes | Yes | Yes | Yes | Yes |
| Archive own notification | Yes | Yes | Yes | Yes | Yes |
| Create internal notification | Services only | No | No | No | No |
| View notification status internally | Services only | No | No | No | No |

This authorization matrix is a Project A contract decision.

Authorization must combine:

```text
JWT identity
+
role
+
business relationship/scope
```

---

# 71. Visitor Workflow

Canonical visitor workflow:

```text
Resident/Owner
      |
      | create visitor request
      v
PENDING
      |
      | Security/Manager
      v
APPROVED
      |
      | Security
      v
CHECKED_IN
      |
      | Security
      v
CHECKED_OUT
```

Alternative:

```text
PENDING
   |
   +----> REJECTED
   |
   +----> CANCELLED
```

A notification can be created when important visitor events occur.

---

# 72. Announcement Workflow

```text
Manager/Admin
      |
      | create
      v
DRAFT
      |
      | optional schedule
      v
SCHEDULED
      |
      v
PUBLISHED
      |
      v
EXPIRED
```

Cancellation can occur at allowed lifecycle states.

Publication may trigger:

```text
Notification
```

for the configured audience.

---

# 73. Notification Workflow

```text
Source Service
      |
      | create notification
      v
Community
      |
      v
DELIVERED / UNREAD
      |
      | user opens
      v
READ
      |
      | optional archive
      v
ARCHIVED
```

Notification creation must not depend on a real SMS/email provider.

---

# 74. Notification Failure

A failed notification call must not corrupt the originating domain transaction.

Example:

```text
Booking approved
     |
     +---- persist booking
     |
     +---- request notification
```

If notification creation fails:

```text
booking remains approved
```

and the notification failure must be handled explicitly.

Possible approaches:

```text
retry
record failed dispatch
reprocess
```

The project may select the implementation.

Do not silently report notification success when Community did not persist it.

---

# 75. Notification Idempotency

Cross-service notification creation should support an idempotency strategy.

Recommended:

```text
sourceService
sourceEntityType
sourceEntityId
notificationType
recipientUserId
```

can form a deduplication key when the event is intended to occur once.

This prevents retries from creating duplicate notifications.

The exact uniqueness rule depends on the event semantics.

For example:

```text
BOOKING_APPROVED
```

should normally produce one approval notification per recipient.

---

# 76. Announcement Audience and Identity

Community should not copy:

```text
firstName
lastName
email
phone
```

for every recipient merely to implement announcements.

Use authoritative identifiers and service APIs.

Example:

```text
role-based audience
```

can be evaluated using the identity contract.

Example:

```text
unit-based audience
```

can use resident/property relationship APIs.

---

# 77. Privacy

The case explicitly requires synthetic data only and prohibits publishing real resident, contact, financial, or visitor information. fileciteturn67file7

Community must therefore:

- use synthetic visitor names/contact values;
- avoid unnecessary PII in API responses;
- avoid exposing visitor data to unrelated residents;
- avoid logging visitor contact details unnecessarily;
- avoid returning complete user profiles in announcement audience responses;
- protect notification content according to recipient scope.

---

# 78. Database Ownership

Database:

```text
community_db
```

Recommended tables:

```text
visitors
visitor_history
announcements
announcement_audiences
announcement_reads
notifications
```

Optional:

```text
notification_dispatch_history
```

if delivery/retry tracking is implemented.

No cross-service foreign keys to:

```text
identity_access_db
resident_management_db
property_unit_db
lease_occupancy_db
billing_payment_db
utility_charge_db
operations_db
```

External IDs are references, not cross-service database foreign keys.

---

# 79. Visitor Table Concept

Possible fields:

```text
id
resident_user_id
unit_id
visitor_name
visitor_contact
visit_date
expected_arrival
expected_departure
purpose
status
check_in_at
check_out_at
notes
created_by_user_id
created_at
updated_at
```

Use UUIDs for IDs.

Do not create local authoritative copies of User or Unit entities.

---

# 80. Announcement Tables

Possible:

```text
announcements
announcement_audiences
announcement_reads
```

`announcement_audiences` may contain:

```text
id
announcement_id
audience_type
role_code
unit_id
```

depending on the selected targeting model.

The exact database schema is a Project A implementation decision.

---

# 81. Notification Table

Possible:

```text
notifications
```

Fields:

```text
id
recipient_user_id
type
title
message
priority
status
delivery_status
source_service
source_entity_type
source_entity_id
read_at
created_at
expires_at
```

The service must ensure notification recipient scope.

---

# 82. Transactions

Visitor creation:

```text
BEGIN
    validate local input
    persist visitor
    persist history
COMMIT
```

Visitor approval:

```text
BEGIN
    load visitor
    validate state
    update state
    persist history
COMMIT
```

Announcement publication:

```text
BEGIN
    validate content/audience
    update status
    persist publication event
COMMIT
```

Notification creation:

```text
BEGIN
    validate notification
    enforce idempotency
    persist notification
COMMIT
```

Local state changes must be atomic.

---

# 83. Cross-Service Dependency Failure

If a required Identity/Resident/Property dependency is unavailable:

```http
503 Service Unavailable
```

with:

```text
DEPENDENCY_UNAVAILABLE
```

must be returned.

Do not:

- assume requester is a valid resident;
- assume unit exists;
- create visitor records using unverified relationships;
- expose internal dependency details.

Recommended starting timeouts:

```text
connection timeout: 2 seconds
read timeout:       5 seconds
```

---

# 84. Standard Success Response

Use the Project A standard:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {},
  "timestamp": "2026-10-01T10:00:00Z",
  "requestId": "uuid"
}
```

List endpoints may add pagination.

---

# 85. Standard Error Response

```json
{
  "success": false,
  "message": "Unable to process request",
  "error": {
    "code": "VALIDATION_ERROR",
    "details": null
  },
  "timestamp": "2026-10-01T10:00:00Z",
  "requestId": "uuid"
}
```

Do not expose:

```text
stack traces
SQL errors
internal hostnames
JWT details
secrets
```

---

# 86. Error Codes

Recommended stable codes:

| Code | Meaning |
|---|---|
| `VISITOR_NOT_FOUND` | Visitor not found |
| `DUPLICATE_VISITOR_REQUEST` | Duplicate active visitor request |
| `VISITOR_STATUS_TRANSITION_NOT_ALLOWED` | Invalid visitor lifecycle transition |
| `VISITOR_NOT_ELIGIBLE` | Requester/visit does not satisfy eligibility |
| `ANNOUNCEMENT_NOT_FOUND` | Announcement not found |
| `ANNOUNCEMENT_STATUS_TRANSITION_NOT_ALLOWED` | Invalid announcement transition |
| `ANNOUNCEMENT_AUDIENCE_REQUIRED` | Audience not configured |
| `ANNOUNCEMENT_NOT_VISIBLE` | User is outside announcement audience |
| `NOTIFICATION_NOT_FOUND` | Notification not found |
| `NOTIFICATION_ACCESS_DENIED` | Notification belongs to another user |
| `NOTIFICATION_DUPLICATE` | Duplicate notification event |
| `INVALID_NOTIFICATION_SOURCE` | Invalid source service/entity reference |
| `UNIT_NOT_FOUND` | Unit not found |
| `USER_NOT_FOUND` | User not found |
| `DEPENDENCY_UNAVAILABLE` | Required downstream service unavailable |
| `VALIDATION_ERROR` | Request validation failure |
| `UNAUTHORIZED` | Authentication failure |
| `FORBIDDEN` | Authorization failure |
| `INTERNAL_SERVER_ERROR` | Unexpected service failure |

---

# 87. HTTP Status Codes

| HTTP | Meaning |
|---|---|
| `200` | Successful operation |
| `201` | Resource created |
| `202` | Accepted operation where appropriate |
| `204` | Success without body |
| `400` | Invalid request |
| `401` | Authentication failure |
| `403` | Authorization failure |
| `404` | Resource not found |
| `409` | Business conflict |
| `422` | Semantically invalid request |
| `429` | Rate limited |
| `500` | Unexpected service failure |
| `503` | Required dependency unavailable |

---

# 88. Pagination

Collection endpoints should support:

```text
page=0
size=20
```

Recommended maximum:

```text
100
```

Applicable endpoints:

```text
GET /visitors
GET /announcements
GET /notifications
GET /notifications/history
```

---

# 89. Request Headers

Use:

```http
Content-Type: application/json
Accept: application/json
Authorization: Bearer <JWT>
X-Request-ID: <UUID>
```

Propagate:

```text
X-Request-ID
```

to downstream calls.

---

# 90. Date and Time Rules

Use:

```text
ISO-8601
UTC
```

for API timestamps.

Example:

```text
2026-10-10T18:00:00Z
```

Visitor date/time inputs must use a documented apartment-local timezone when local visit schedules are represented.

Server-generated timestamps are authoritative for:

```text
createdAt
updatedAt
checkInAt
checkOutAt
publishedAt
readAt
```

---

# 91. Logging

Logs should include:

```text
timestamp
service
requestId
userId
operation
result
errorCode
```

For internal calls:

```text
callingService
```

may also be logged.

Never log:

```text
JWTs
passwords
private keys
secrets
Authorization headers
database credentials
```

Visitor contact data should not be logged unless necessary for debugging and then only with appropriate masking.

---

# 92. Audit Events

Important Community events include:

```text
VISITOR_CREATED
VISITOR_UPDATED
VISITOR_APPROVED
VISITOR_REJECTED
VISITOR_CHECKED_IN
VISITOR_CHECKED_OUT
VISITOR_CANCELLED
VISITOR_EXPIRED

ANNOUNCEMENT_CREATED
ANNOUNCEMENT_UPDATED
ANNOUNCEMENT_PUBLISHED
ANNOUNCEMENT_CANCELLED
ANNOUNCEMENT_EXPIRED
ANNOUNCEMENT_READ

NOTIFICATION_CREATED
NOTIFICATION_READ
NOTIFICATION_ARCHIVED
NOTIFICATION_DISPATCH_FAILED
```

Audit records should include:

```text
event
entityId
previousState
newState
reason
changedBy
changedAt
requestId
```

---

# 93. Notification Audit

For internal notification creation, retain:

```text
sourceService
sourceEntityType
sourceEntityId
recipientUserId
notificationType
createdAt
```

This allows troubleshooting without requiring Community to query the source service database.

---

# 94. API Gateway Routes

Public:

```text
/api/v1/visitors/**
/api/v1/announcements/**
/api/v1/notifications/**
```

Internal:

```text
/api/v1/internal/notifications/**
```

Operational:

```text
/actuator/health
/actuator/info
```

The API Gateway remains the central routing and trust point.

---

# 95. OpenAPI / Swagger

Required:

```text
/swagger-ui.html
/v3/api-docs
```

Every endpoint must document:

- API ID;
- HTTP method;
- URI;
- purpose;
- authentication;
- roles;
- business scope;
- path parameters;
- query parameters;
- request body;
- validation;
- success response;
- errors;
- business error codes;
- dependencies;
- failure behavior;
- examples.

The OpenAPI contract must match the implementation exactly.

The shared API standard requires OpenAPI/Swagger documentation for every endpoint. fileciteturn68file7

---

# 96. Testing Requirements

## 96.1 Visitor tests

Test:

- required visitor name;
- valid unit;
- requester relationship;
- visitor creation;
- duplicate request behavior;
- approval;
- rejection;
- check-in;
- check-out;
- invalid transitions;
- cancellation;
- privacy/access scope.

## 96.2 Announcement tests

Test:

- required title/content;
- audience requirement;
- creation;
- update;
- publication;
- cancellation;
- expiry;
- role targeting;
- unit targeting;
- visibility filtering;
- read state;
- unauthorized publication.

## 96.3 Notification tests

Test:

- internal creation;
- recipient isolation;
- notification retrieval;
- read;
- read-all;
- archive;
- duplicate prevention;
- source metadata;
- invalid service caller;
- invalid notification type;
- notification status.

## 96.4 Security tests

Test:

- missing JWT;
- invalid JWT;
- expired JWT;
- invalid signature;
- wrong token type;
- insufficient role;
- resident accessing another resident's visitor;
- resident accessing another user's notification;
- unauthorized announcement publication;
- unauthorized internal API access.

## 96.5 Integration tests

Verify:

```text
Community
    ↓
Identity
```

```text
Community
    ↓
Resident Management
```

```text
Community
    ↓
Property Unit
```

```text
Operations
    ↓
Community Notification API
```

and, where needed:

```text
Billing
    ↓
Community Notification API
```

---

# 97. Required End-to-End Demonstrations

## Visitor workflow

Demonstrate:

```text
resident login
   ↓
valid unit relationship
   ↓
visitor request created
   ↓
security/manager approves
   ↓
visitor checks in
   ↓
visitor checks out
   ↓
resident/security can view history
```

This directly supports the required visitor-handling workflow. fileciteturn67file2

## Announcement workflow

Demonstrate:

```text
manager login
   ↓
create announcement
   ↓
configure audience
   ↓
publish
   ↓
eligible resident sees announcement
   ↓
resident marks announcement read
```

## Notification workflow

Demonstrate:

```text
Operations
   |
   | booking approved
   v
Community internal notification API
   |
   v
Resident notification appears
   |
   v
Resident marks notification read
```

The case requires in-application notifications for important workflow events. fileciteturn67file0

---

# 98. Postman

Provide Postman requests/tests for:

```text
POST /api/v1/visitors
GET /api/v1/visitors
GET /api/v1/visitors/{visitorId}
PATCH /api/v1/visitors/{visitorId}
POST /api/v1/visitors/{visitorId}/approve
POST /api/v1/visitors/{visitorId}/reject
POST /api/v1/visitors/{visitorId}/check-in
POST /api/v1/visitors/{visitorId}/check-out

POST /api/v1/announcements
GET /api/v1/announcements
GET /api/v1/announcements/{announcementId}
PATCH /api/v1/announcements/{announcementId}
POST /api/v1/announcements/{announcementId}/publish
POST /api/v1/announcements/{announcementId}/cancel
POST /api/v1/announcements/{announcementId}/read
GET /api/v1/announcements/{announcementId}/audience-preview

GET /api/v1/notifications
GET /api/v1/notifications/{notificationId}
POST /api/v1/notifications/{notificationId}/read
POST /api/v1/notifications/read-all
GET /api/v1/notifications/summary
GET /api/v1/notifications/history
POST /api/v1/notifications/{notificationId}/archive

POST /api/v1/internal/notifications
GET /api/v1/internal/notifications/{notificationId}/status
POST /api/v1/internal/notifications/resident
```

Also test:

```text
401
403
404
409
503
```

where applicable.

---

# 99. Repository Standard

Expected:

```text
community-service/
├── .ai/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── kln/ams/community/
│   │   └── resources/
│   └── test/
│       └── java/
│           └── kln/ams/community/
├── docs/
│   └── openapi/
├── Dockerfile
├── pom.xml
├── README.md
├── .env.example
└── .gitignore
```

Recommended packages:

```text
kln.ams.community
├── controller
├── service
├── repository
├── entity
├── dto
├── mapper
├── validation
├── exception
├── security
├── client
├── config
├── audit
└── notification
```

---

# 100. Repository Cleanup Rules

Before implementation:

1. Inspect the complete repository.
2. Find all visitor controllers.
3. Find all announcement controllers.
4. Find all notification controllers.
5. Find all entities.
6. Find all repositories.
7. Find all DTOs.
8. Find all service clients.
9. Find all database migrations.
10. Find all tests.
11. Find all OpenAPI assets.
12. Find all Postman assets.
13. Find all security configuration.
14. Find all notification implementations in other packages.

Then:

15. Remove duplicate Visitor resources.
16. Remove duplicate Announcement resources.
17. Remove duplicate Notification resources.
18. Remove notification ownership from `operations-service`.
19. Remove direct cross-service database access.
20. Remove obsolete visitor APIs.
21. Remove conflicting announcement lifecycle APIs.
22. Remove conflicting notification models.
23. Normalize role and relationship authorization.
24. Create missing canonical APIs.
25. Update persistence.
26. Update security.
27. Update service clients.
28. Update tests.
29. Update OpenAPI.
30. Update Postman.
31. Update Gateway configuration.
32. Compile.
33. Run tests.
34. Run integration tests.
35. Verify visitor workflow.
36. Verify announcement workflow.
37. Verify notification workflow.
38. Verify dependency failure behavior.
39. Verify security.
40. Verify Swagger.
41. Verify health.
42. Remove obsolete/temp files.
43. Leave a clean repository state.

Do not preserve an obsolete API merely because another old component uses it.

Update the dependent component to the canonical contract.

---

# 101. Forbidden Patterns

Do not:

- access another service's database;
- duplicate User/Resident/Unit/Occupancy as authoritative data;
- create a second Notification owner;
- create Notifications inside Operations;
- create Visitor inside Operations;
- create Announcement inside Operations;
- implement real SMS/email delivery;
- bypass the API Gateway for normal service-to-service traffic;
- trust unit relationship from the request body alone;
- allow residents to view unrelated visitor records;
- allow residents to view unrelated notifications;
- allow unauthorized users to publish announcements;
- return fake success when a dependency is unavailable;
- log JWTs;
- expose secrets;
- return stack traces;
- use uncontrolled status strings;
- silently change another service's contract.

---

# 102. Cross-Service Notification Contract

The most important Community integration contract is:

```text
Provider:
community-service

Consumer:
operations-service
billing-payment-service
utility-charge-service
lease-occupancy-service
other registered Project A services when documented
```

Provider API:

```http
POST /api/v1/internal/notifications
```

The consumer sends:

```text
recipient
type
title
message
priority
source service
source entity
```

Community persists:

```text
Notification
```

The consumer remains authoritative for its own domain event.

---

# 103. Example — Booking Approval Notification

Operations owns:

```text
Booking
```

Operations approves:

```text
Booking = APPROVED
```

Operations then calls:

```http
POST /api/v1/internal/notifications
```

with:

```json
{
  "recipientUserId": "uuid",
  "type": "BOOKING_APPROVED",
  "title": "Booking approved",
  "message": "Your facility booking has been approved.",
  "priority": "NORMAL",
  "sourceService": "operations-service",
  "sourceEntityType": "Booking",
  "sourceEntityId": "uuid"
}
```

Community creates:

```text
Notification
```

The frontend retrieves it through:

```http
GET /api/v1/notifications
```

No Booking record is created in Community.

---

# 104. Example — Maintenance Update Notification

Operations owns:

```text
MaintenanceRequest
```

When status changes:

```text
IN_PROGRESS
```

Operations may request:

```text
MAINTENANCE_REQUEST_UPDATED
```

Community stores:

```text
sourceService = operations-service
sourceEntityType = MaintenanceRequest
sourceEntityId = requestId
```

The resident can see the notification but Community does not calculate maintenance status.

---

# 105. Example — Announcement Notification

When an announcement is published:

```text
Announcement
      |
      v
PUBLISHED
      |
      v
Community determines audience
      |
      v
Notification(s)
```

The notification recipient list is derived from the announcement audience.

For a prototype, notification creation may be synchronous.

If the implementation later uses asynchronous processing, the API contract must remain explicit and documented.

---

# 106. Announcement Publication Consistency

Publication must be locally atomic:

```text
announcement status
+
publication timestamp
+
audit event
```

must be committed consistently.

Notification generation may occur after publication.

If notification generation fails:

```text
Announcement remains PUBLISHED
```

and notification processing must be retried/reported.

Do not roll back a successfully published announcement merely because a notification failed unless the project explicitly chooses a transactional messaging architecture.

---

# 107. Visitor Notification Examples

Possible events:

```text
VISITOR_APPROVED
VISITOR_REJECTED
VISITOR_CHECKED_IN
VISITOR_CHECKED_OUT
```

A visitor notification should be created only for a meaningful recipient.

Example:

```text
Security checks in visitor
        |
        v
Community
        |
        v
Resident notification
```

Avoid notifying unrelated users.

---

# 108. Announcement Visibility

For normal users:

```text
GET /api/v1/announcements
```

must apply audience filtering.

Example:

```text
Announcement A
audience = OWNER
```

A `TENANT_RESIDENT` must not receive it unless another configured audience rule also includes that user.

Example:

```text
Announcement B
audience = ALL_RESIDENTS
```

Both owners and tenant/residents may see it according to the Project A audience definition.

---

# 109. Notification Visibility

For normal users:

```text
GET /api/v1/notifications
```

is always scoped to the authenticated user.

Do not support a normal-user query such as:

```http
GET /api/v1/notifications?recipientUserId=<other-user>
```

as an authorization bypass.

If management requires an administrative notification-report API later, it must be explicitly added to the Project A contract.

---

# 110. Security Officer Scope

The canonical Project A role:

```text
SECURITY_OFFICER
```

primarily supports:

```text
visitor management
visitor approval
visitor rejection
visitor check-in
visitor check-out
```

It must not automatically gain:

```text
announcement publishing
billing access
user administration
role administration
```

unless separately authorized.

The case identifies the Security Officer as responsible for recording or validating visitors and viewing approved visitor information. fileciteturn67file2

---

# 111. Apartment Manager Scope

The Apartment Manager can manage community information needed for apartment administration, including:

```text
visitor records
announcements
notifications/workflow visibility
```

subject to the final authorization matrix.

The manager does not become owner of:

```text
User
Resident
Unit
Invoice
Payment
```

---

# 112. Role Model

Use the Project A canonical roles:

```text
SYSTEM_ADMINISTRATOR
APARTMENT_MANAGER
OWNER
TENANT_RESIDENT
FINANCE_OFFICER
MAINTENANCE_COORDINATOR
TECHNICIAN
SERVICE_STAFF
SECURITY_OFFICER
```

Primary Community roles:

```text
SYSTEM_ADMINISTRATOR
APARTMENT_MANAGER
SECURITY_OFFICER
OWNER
TENANT_RESIDENT
```

Do not introduce new roles such as:

```text
COMMUNITY_ADMIN
ANNOUNCEMENT_MANAGER
VISITOR_MANAGER
NOTIFICATION_ADMIN
```

unless the project-wide role contract is explicitly changed.

---

# 113. API Versioning

Canonical:

```text
/api/v1
```

Breaking changes require:

```text
/api/v2
```

Do not silently change a `v1` request/response contract.

---

# 114. UUID and Naming Rules

IDs:

```text
UUID
```

JSON:

```text
camelCase
```

URLs:

```text
kebab-case
```

Resources:

```text
plural
```

Examples:

```text
visitors
announcements
notifications
```

Enums:

```text
UPPER_SNAKE_CASE
```

---

# 115. Performance Expectations

The assignment expects reasonable prototype performance under normal classroom-scale load.

Community should measure common operations:

```text
GET /visitors
GET /announcements
GET /notifications
GET /notifications/summary
POST /visitors
POST /announcements/{id}/publish
POST /internal/notifications
```

Record actual observations.

Do not invent performance measurements.

---

# 116. Reliability Requirements

Required:

- dependency failure handling;
- no corrupted local data;
- valid lifecycle transitions;
- notification idempotency;
- request ID propagation;
- privacy-aware authorization;
- audit-friendly history;
- clear errors.

The project scope requires understandable dependent-service failures and protection against local data corruption. fileciteturn67file7

---

# 117. Standard Exception Handling

Use:

```java
@RestControllerAdvice
```

Recommended exceptions:

```text
ResourceNotFoundException
BusinessRuleViolationException
InvalidStatusTransitionException
UnauthorizedException
ForbiddenException
DependencyUnavailableException
DuplicateNotificationException
```

Every controller must use the common Project A error envelope.

---

# 118. Cross-Service Data References

Community may store:

```text
userId
residentUserId
unitId
sourceEntityId
```

as UUID references.

These do not make Community the owner of the referenced domain.

For example:

```text
Visitor.unitId
```

means:

```text
reference to Property Unit Service
```

not:

```text
Community-owned Unit
```

---

# 119. Data Retention

Historical records should remain available according to the project's prototype requirements.

Do not automatically hard-delete:

```text
visitor history
announcement history
notification history
```

merely because the record is no longer active.

If retention/cleanup is implemented, it must be explicitly documented.

---

# 120. Notification Expiry

Notifications may contain:

```text
expiresAt
```

for time-sensitive information.

Expired notifications may remain historically stored but should not appear in the default active feed if the contract defines them as expired.

Expiry must not be confused with read state.

---

# 121. Announcement Expiry

When:

```text
expiresAt < current time
```

the announcement may transition to:

```text
EXPIRED
```

according to the implementation.

Expired announcements remain stored for authorized history/reporting.

---

# 122. Visitor Check-In Rules

Check-in must verify:

```text
status == APPROVED
```

and may verify:

```text
current date/time within allowed visit window
```

if that rule is enabled.

The security officer must not be able to check in:

```text
REJECTED
CANCELLED
EXPIRED
CHECKED_OUT
```

visitors.

---

# 123. Visitor Check-Out Rules

Check-out requires:

```text
status == CHECKED_IN
```

The server records:

```text
checkOutAt = current server time
```

The operation should be idempotent only if the project explicitly chooses that behavior; otherwise repeated checkout should return a clear lifecycle error.

---

# 124. Announcement Publication Rules

Before publishing:

```text
title != empty
content != empty
audience configured
status in publishable state
```

After publication:

```text
publishedAt = server time
status = PUBLISHED
```

Audience filtering must be applied to readers.

---

# 125. Notification Idempotency Example

If Operations retries:

```text
POST /api/v1/internal/notifications
```

after a timeout, Community should not automatically create two notifications if the same event was already persisted.

Recommended logical key:

```text
operations-service
+
Booking
+
bookingId
+
BOOKING_APPROVED
+
recipientUserId
```

This is a Project A implementation decision.

---

# 126. Internal Caller Authorization

Community internal notification APIs should maintain an allow-list of registered service identities.

Example:

```text
operations-service
billing-payment-service
utility-charge-service
lease-occupancy-service
```

Only services explicitly registered in the project contract may call the endpoint.

Do not authorize based only on:

```text
sourceService
```

inside the JSON request body.

The authenticated service identity is authoritative.

---

# 127. Source Service Consistency

If the JWT says:

```text
sub = operations-service
```

but the request body says:

```json
{
  "sourceService": "billing-payment-service"
}
```

the request must not be silently accepted.

Either:

```text
reject
```

or:

```text
derive sourceService from authenticated caller
```

The implementation should preferably derive authoritative caller identity from the service JWT and validate any supplied source metadata against it.

---

# 128. Notification Source Entity

The source entity is informational and traceable.

Example:

```text
sourceService = operations-service
sourceEntityType = Booking
sourceEntityId = 6f...
```

Community must not attempt to reconstruct the Booking.

If frontend needs Booking details, it calls:

```text
operations-service
```

through the appropriate API.

---

# 129. Internal API Failure Behavior

If an internal notification creation request is invalid:

```http
400
```

If caller is unauthenticated:

```http
401
```

If caller is authenticated but not registered/authorized:

```http
403
```

If recipient validation dependency is unavailable:

```http
503
```

If duplicate notification is detected:

```http
409
```

---

# 130. Gateway Integration

The Gateway should route:

```text
/api/v1/visitors/**
/api/v1/announcements/**
/api/v1/notifications/**
```

to Community.

Internal:

```text
/api/v1/internal/notifications/**
```

must be restricted to service-to-service callers.

Community must not rely on frontend code to enforce these restrictions.

---

# 131. Frontend Integration

Owned frontend routes:

```text
/visitors
/announcements
/notifications
```

Community APIs should support:

```text
visitor management UI
announcement management UI
announcement feed
notification center
notification badge/unread count
```

The shared frontend must access these APIs through the API Gateway.

---

# 132. Resident Dashboard Notification

The notification summary endpoint supports:

```text
unreadCount
```

for a notification badge.

Example:

```text
Header
  🔔 5
```

Frontend should call:

```http
GET /api/v1/notifications/summary
```

using the authenticated user's JWT.

---

# 133. Resident Announcement Feed

The announcement list should support:

```text
published announcements
audience filtering
pagination
priority/category filtering
read state
```

A resident must not need to download all announcements and filter locally.

Audience filtering belongs to the Community backend.

---

# 134. Visitor Dashboard

A resident may see:

```text
pending visitors
approved visitors
checked-in visitors
past visits
```

using:

```http
GET /api/v1/visitors
```

with backend-enforced relationship scope.

---

# 135. Security Logging

Security-sensitive operations should be auditable:

```text
visitor approved
visitor rejected
visitor checked in
visitor checked out
announcement published
announcement cancelled
notification created
```

The log should identify:

```text
actor userId
role
operation
entity
requestId
timestamp
```

Avoid logging unnecessary visitor PII.

---

# 136. Database Migration Rules

Use versioned migrations.

Examples:

```text
V1__create_visitors.sql
V2__create_visitor_history.sql
V3__create_announcements.sql
V4__create_announcement_audiences.sql
V5__create_announcement_reads.sql
V6__create_notifications.sql
```

Actual migration tool may be Flyway or another project-approved mechanism.

Never edit an already-applied production/shared migration without following the project's migration policy.

---

# 137. Test Data

Use synthetic data only.

Example visitor:

```text
Visitor Name: Nimal Perera
Contact: 0770000000
```

Do not use actual resident information.

Seed data must not contain:

```text
real phone numbers
real addresses
real identity numbers
real financial data
```

---

# 138. README Requirements

Repository README should include:

1. Service overview
2. Project information
3. Responsibilities
4. Technology stack
5. Architecture
6. Repository structure
7. API documentation
8. Authentication
9. Endpoint list
10. Dependencies
11. Configuration
12. Local setup
13. Database
14. Swagger
15. Testing
16. Docker
17. Health
18. Error handling
19. Development guidelines
20. Related services
21. Contract changes
22. Ownership

---

# 139. AI Implementation Agent Workflow

The implementation agent must execute:

```text
INSPECT
   ↓
PLAN
   ↓
IMPLEMENT
   ↓
COMPILE
   ↓
TEST
   ↓
FIX
   ↓
RUN
   ↓
VERIFY
   ↓
DOCUMENT
   ↓
FINAL CHECK
```

Verification must cover:

```text
database
security
API contracts
Gateway
dependencies
business rules
privacy
tests
Swagger
health
Docker
```

---

# 140. Final Verification Checklist

## Contract

- [ ] All visitor endpoints exist.
- [ ] All announcement endpoints exist.
- [ ] All notification endpoints exist.
- [ ] All internal notification APIs exist.
- [ ] Standard response envelope is used.
- [ ] Standard error envelope is used.
- [ ] OpenAPI matches implementation.
- [ ] No duplicate Notification API remains elsewhere in Group 4.
- [ ] No duplicate Visitor resource remains in Operations.
- [ ] No duplicate Announcement resource remains in Operations.
- [ ] No obsolete Community API remains.

## Visitors

- [ ] Resident can create visitor request.
- [ ] Unit is validated.
- [ ] Resident/unit relationship is validated.
- [ ] Security officer can approve.
- [ ] Security officer can reject.
- [ ] Approved visitor can be checked in.
- [ ] Checked-in visitor can be checked out.
- [ ] Invalid status transitions are rejected.
- [ ] Visitor history is auditable.
- [ ] Unauthorized users cannot view unrelated visitors.
- [ ] Synthetic visitor data is used.

## Announcements

- [ ] Manager can create announcement.
- [ ] Audience is required.
- [ ] Manager/admin can publish.
- [ ] Published announcements are audience-filtered.
- [ ] Read state is per user.
- [ ] Cancellation works.
- [ ] Expiry works.
- [ ] Unauthorized users cannot publish.
- [ ] Audience preview does not expose unnecessary PII.

## Notifications

- [ ] Authenticated users can view own notifications.
- [ ] Users cannot access another user's notifications.
- [ ] Internal services can create notifications.
- [ ] Internal caller authorization works.
- [ ] Notification source metadata is stored.
- [ ] Duplicate events are handled.
- [ ] Read works.
- [ ] Read-all works.
- [ ] Archive works.
- [ ] Summary/unread count works.
- [ ] Notification failure does not corrupt source transactions.
- [ ] No SMS/email dependency exists.

## Security

- [ ] Gateway JWT is verified.
- [ ] RS256 is used.
- [ ] Token type is checked.
- [ ] Role restrictions work.
- [ ] Business relationship scope works.
- [ ] Internal endpoints are service-only.
- [ ] JWTs are not logged.
- [ ] Secrets are not committed.

## Integration

- [ ] Identity validation works.
- [ ] Resident relationship validation works.
- [ ] Property unit validation works.
- [ ] Occupancy validation works where required.
- [ ] Operations notification integration works.
- [ ] Optional Billing/Utility notification integrations are explicit.
- [ ] Dependency failure returns `503 DEPENDENCY_UNAVAILABLE`.
- [ ] No cross-service database access exists.

## Testing

- [ ] Unit tests pass.
- [ ] API tests pass.
- [ ] Integration tests pass.
- [ ] Security tests pass.
- [ ] Visitor workflow test passes.
- [ ] Announcement workflow test passes.
- [ ] Notification workflow test passes.
- [ ] Postman tests pass.
- [ ] Performance observations are recorded.

## Deployment

- [ ] Docker image builds.
- [ ] Database migrations run.
- [ ] Health endpoint works.
- [ ] Swagger works.
- [ ] Gateway routes work.
- [ ] `.env.example` contains no secrets.
- [ ] Repository has no obsolete temporary implementation.
- [ ] Repository has clean status.

---

# 141. Source-to-Contract Notes

## Directly supported by the Project A sources

The sources support:

- Group 4 ownership of operations and community;
- `community-service`;
- visitor records;
- visitor/unit/resident reference;
- visitor date, purpose, status, and basic check-in information;
- announcements;
- role-targeted announcements;
- read/visibility status where feasible;
- in-application notifications;
- role-restricted visitor and announcement access;
- identity/resident dependency;
- unit status dependency;
- selected workflow notifications;
- synthetic data/privacy constraints;
- no real SMS/email delivery. fileciteturn67file0 fileciteturn67file2 fileciteturn67file7

## Project A contract decisions

The following are detailed integration decisions made for this contract:

- endpoint inventory;
- API IDs;
- exact URI shapes;
- exact request/response fields;
- status enums;
- notification types;
- notification priorities;
- audience types;
- authorization matrix;
- internal notification APIs;
- error codes;
- audit schema;
- notification idempotency strategy;
- exact visitor duplicate rules;
- exact expiry behavior.

These decisions must remain synchronized with:

```text
00-PROJECT-A-CONTRACT-DECISIONS.md
01-PROJECT-A-GLOBAL-API-STANDARD.md
02-PROJECT-A-JWT-SECURITY-STANDARD.md
03-PROJECT-A-CROSS-SERVICE-API-REGISTRY.md

10-IDENTITY-ACCESS-SERVICE.md
11-RESIDENT-MANAGEMENT-SERVICE.md
12-PROPERTY-UNIT-SERVICE.md
13-LEASE-OCCUPANCY-SERVICE.md
14-BILLING-PAYMENT-SERVICE.md
15-UTILITY-CHARGE-SERVICE.md
16-OPERATIONS-SERVICE.md
18-API-GATEWAY.md
```

---

# 142. Canonical Rule for the AI Implementation Agent

```text
The existing repository is NOT the source of truth.

COMMUNITY-SERVICE.md is the target contract.

Inspect the complete repository before editing.

Identify controllers, services, repositories, entities, DTOs,
security configuration, database migrations, tests, OpenAPI,
configuration, service clients, and existing API routes.

Compare the complete implementation against this contract.

Remove obsolete APIs.
Remove duplicate APIs.
Remove conflicting APIs.
Remove duplicate visitor resources.
Remove duplicate announcement resources.
Remove duplicate notification resources.
Remove notification ownership from Operations.
Remove direct cross-service database access.
Modify incompatible implementation.
Create missing visitor functionality.
Create missing announcement functionality.
Create missing notification functionality.
Create missing internal notification APIs.
Update database migrations.
Update security.
Update service clients.
Update tests.
Update OpenAPI.
Update Postman.
Update Gateway routing.
Compile.
Run tests.
Run integration verification.
Verify visitor workflow.
Verify announcement workflow.
Verify notification workflow.
Verify database behavior.
Verify Gateway routing.
Verify internal JWT authentication.
Verify dependency failure behavior.
Verify authorization scope.
Verify privacy controls.
Verify notification idempotency.
Verify Swagger.
Verify health.
Remove temporary/obsolete artifacts.

Do not preserve an old API merely because it exists.

Do not invent an alternative service boundary.

Do not duplicate Operations-owned MaintenanceRequest,
WorkOrder, Assignment, Facility, or Booking resources.

Do not duplicate Identity-owned User/Role resources.

Do not duplicate Resident-owned resident/owner/tenant/staff resources.

Do not directly access another service's database.

Do not silently change another service's API.

The final repository must be a clean, synchronized implementation
of the Project A canonical Community service contract.
```

---

# 143. Related Project Documents

```text
00-PROJECT-A-CONTRACT-DECISIONS.md
01-PROJECT-A-GLOBAL-API-STANDARD.md
02-PROJECT-A-JWT-SECURITY-STANDARD.md
03-PROJECT-A-CROSS-SERVICE-API-REGISTRY.md

10-IDENTITY-ACCESS-SERVICE.md
11-RESIDENT-MANAGEMENT-SERVICE.md
12-PROPERTY-UNIT-SERVICE.md
13-LEASE-OCCUPANCY-SERVICE.md
14-BILLING-PAYMENT-SERVICE.md
15-UTILITY-CHARGE-SERVICE.md
16-OPERATIONS-SERVICE.md
18-API-GATEWAY.md
```

---

# END — COMMUNITY SERVICE CONTRACT
