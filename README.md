# Community Service

Part of the **Apartment Management System (AMS)** — a Spring Boot microservice handling **Facilities, Reservations, Visitors, Announcements, and Notifications** for Group 4 (Operations & Community).

## Tech Stack

- **Java 21**, **Spring Boot 4.1.1** (Maven)
- **MySQL** (default), with **PostgreSQL** and embedded **H2** profiles also available
- **Spring Data JPA** / Hibernate
- **JJWT** (RS256) for Gateway-issued JWT verification
- **springdoc-openapi** for Swagger/OpenAPI docs
- **Docker** + **Docker Compose** for containerized runs
- **GitHub Actions** for CI (Maven build + test on every PR)

## Prerequisites

- JDK 21
- Maven (or use the included `./mvnw` wrapper — no separate install needed)
- MySQL 8.x running locally, **or** Docker, if you'd rather not install MySQL yourself

## Getting Started

### 1. Clone the repo

```bash
git clone https://github.com/ams-mit/community-service.git
cd community-service
```

### 2. Set up environment variables

Copy the example file and fill in your own values:

```bash
cp .env.example .env
```

| Variable | Purpose | Default |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Which database profile to use: `mysql`, `postgres` | `mysql` |
| `SPRING_DATASOURCE_URL` | JDBC URL for your database | — |
| `SPRING_DATASOURCE_USERNAME` | Database username | — |
| `SPRING_DATASOURCE_PASSWORD` | Database password | — |
| `GATEWAY_JWT_PUBLIC_KEY` | Base64 RSA public key from the API Gateway, used to verify incoming JWTs | *(empty — pending from Gateway team)* |
| `JWT_SECURITY_ENABLED` | Turns JWT authentication on/off | `true` |

> **Note:** `.env` is git-ignored and should never be committed. Never commit real database credentials directly into `application*.properties` files.

### 3. Run locally

```bash
./mvnw spring-boot:run
```

The service starts on **http://localhost:8085**.

**If you don't have the Gateway's public key yet**, JWT verification will fail on every protected endpoint. Disable it temporarily while developing:

```bash
# PowerShell
$env:JWT_SECURITY_ENABLED="false"
./mvnw spring-boot:run

# bash/zsh
JWT_SECURITY_ENABLED=false ./mvnw spring-boot:run
```

### 4. Or run with Docker Compose (MySQL included)

```bash
docker compose up --build
```

This starts both the MySQL container and the service together, on ports `3308` (MySQL) and `8085` (app).

## API Documentation

- **Swagger UI:** http://localhost:8085/swagger-ui/index.html
- **OpenAPI JSON:** http://localhost:8085/v3/api-docs
- **Postman Collection:** [`postman/Community Service API.postman_collection.json`](postman/Community%20Service%20API.postman_collection.json) — import into Postman for ready-made example requests.

## Endpoints Overview

| Resource | Base path | Key operations |
|---|---|---|
| Facilities | `/api/v1/facilities` | List, get by id, create, update, change status, soft-delete, check availability |
| Reservations | `/api/v1/facilities/reservations` | List, get by id, create, update status, cancel |
| Visitors | `/api/v1/visitors` | List, get by id, register, check-in, check-out, cancel, verify by pass code |
| Announcements | `/api/v1/announcements` | List (role-filtered), get by id, publish, update, archive, delete |
| Notifications | `/api/v1/notifications` | Create (internal, service-to-service), list, get by id, mark read/read-all, delete |
| Health | `/actuator/health`, `/actuator/info` | Public — no authentication required |

Full request/response schemas are in Swagger; the canonical contract for cross-team integration is `COMMUNITY_SERVICE_CANONICAL_API_REGISTRY.md` in this repo.

## Security

Incoming requests are expected to carry a **Gateway-signed JWT** (`Authorization: Bearer <token>`), verified against the Gateway's RS256 public key. The token's `type` claim distinguishes real users (`user`) from trusted backend services (`service`) — `POST /api/v1/notifications` is service-to-service only.

**Current status:** JWT signature verification is implemented, but full role-based authorization enforcement per endpoint is still in progress. `/actuator/health`, `/actuator/info`, and Swagger routes are always public.

## Running Tests

```bash
./mvnw test
```

Tests use mocked repositories (Mockito) and an in-memory H2 database where needed — no live MySQL connection is required to run the test suite.

## CI/CD

Every pull request triggers `.github/workflows/maven-ci.yml`, which builds the project and runs the full test suite against a MySQL service container.

## Project Structure

```
src/main/java/.../community_service/
├── controller/    # REST endpoints
├── service/       # Business logic
├── repository/    # Spring Data JPA repositories
├── entity/        # JPA entities
├── dto/           # Request/response objects
├── security/      # JWT verification and filter
├── config/        # CORS and other app config
└── exception/     # Global exception handling
```

## Contributing

- Branch from `main` using `feature/<description>` or `fix/<description>`.
- **Direct pushes to `main` are not permitted** — open a pull request and get at least one review before merging.
- Reference the relevant Jira issue key in your commit messages where applicable.
