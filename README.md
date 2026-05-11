# BNR Licensing Portal — Backend API

A licensing management system built for the National Bank of Rwanda. It handles the full lifecycle of financial institution license applications — from submission through review to final approval — with role-based access control and a complete audit trail on every action.

Built by **Eng. Placide** · [ndiplacide7@gmail.com](mailto:ndiplacide7@gmail.com)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Runtime | Java 25 |
| Framework | Spring Boot 4.0.6 |
| Security | Spring Security + JWT (JJWT) |
| Persistence | PostgreSQL 16 + Spring Data JPA + Hibernate 7 |
| Migrations | Flyway 11 |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Tests | JUnit 5 + Testcontainers |
| Build | Maven |

---

## Prerequisites

- Java 25
- Maven 3.9+
- Docker Desktop (for PostgreSQL and tests)

---

## Running Locally

**1. Start the database**
```bash
docker compose up -d
```

**2. Start the application**
```bash
mvn spring-boot:run
```

The API is available at `http://localhost:8080/api`

**3. Explore the API**

Swagger UI → `http://localhost:8080/api/swagger-ui.html`

---

## Seed Data

On first startup the application automatically seeds the database with one user per role and two applications in different states. No manual setup needed.

### Users — all share password `Password1!`

| Email | Role |
|-------|------|
| `admin@bnr.rw` | SYSTEM_ADMIN |
| `applicant@bnr.rw` | APPLICANT |
| `reviewer@bnr.rw` | REVIEWER |
| `approver@bnr.rw` | APPROVER |
| `compliance@bnr.rw` | COMPLIANCE_OFFICER |
| `auditor@bnr.rw` | AUDITOR |

### Applications

| Reg. ID | Institution | Status |
|---------|-------------|--------|
| `123456789` | Kigali Microfinance Ltd | `DRAFT` |
| `987654321` | Rwanda Commercial Bank | `UNDER_REVIEW` |

---

## Authentication

All endpoints except `/auth/**` require a Bearer token.

```
POST /auth/login
Authorization: none

Body: { "email": "applicant@bnr.rw", "password": "Password1!" }

Response: { "accessToken": "...", "refreshToken": "...", "expiresIn": 86400, "user": { ... } }
```

Use the token on every subsequent request:
```
Authorization: Bearer <accessToken>
```

Logout invalidates the token server-side:
```
POST /auth/logout
Authorization: Bearer <token>
```

---

## API Reference

### Applications

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/applications` | APPLICANT | Create a new application (DRAFT) |
| `GET` | `/applications` | All | List applications — filtered by role |
| `GET` | `/applications/{id}` | All | Get application details |
| `PATCH` | `/applications/{id}/submit` | APPLICANT | DRAFT → SUBMITTED |
| `PATCH` | `/applications/{id}/assign?reviewerId=` | REVIEWER, COMPLIANCE_OFFICER | SUBMITTED → UNDER_REVIEW |
| `PATCH` | `/applications/{id}/complete-review` | REVIEWER | UNDER_REVIEW → REVIEW_COMPLETED |
| `PATCH` | `/applications/{id}/decide` | APPROVER | REVIEW_COMPLETED → APPROVED / REJECTED |
| `PATCH` | `/applications/{id}/withdraw` | APPLICANT | Any active state → WITHDRAWN |

**Create application body:**
```json
{
  "registrationId": "123456789",
  "institutionName": "My Bank Ltd",
  "licenseType": "COMMERCIAL_BANK"
}
```

**License types:** `COMMERCIAL_BANK` · `MICROFINANCE_INSTITUTION` · `SAVINGS_AND_CREDIT_COOPERATIVE` · `INSURANCE_COMPANY` · `FOREX_BUREAU` · `PAYMENT_SERVICE_PROVIDER`

**Decide body:**
```json
{
  "decision": "APPROVED",
  "decisionNotes": "All requirements met."
}
```

### Documents

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `POST` | `/applications/{id}/documents` | APPLICANT | Upload a document (multipart/form-data) |
| `GET` | `/applications/{id}/documents` | All | List documents for an application |
| `GET` | `/applications/{id}/documents/{docId}` | All | Get a specific document |

**Upload fields:** `file` (File) · `documentType` (Text)

**Document types:** `BUSINESS_PLAN` · `INCORPORATION_CERTIFICATE` · `FINANCIAL_STATEMENTS` · `BOARD_RESOLUTION` · `REGULATORY_COMPLIANCE_REPORT` · `IDENTITY_DOCUMENT` · `OTHER`

### Administration

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| `GET` | `/admin/users` | SYSTEM_ADMIN | List all users |
| `POST` | `/admin/users` | SYSTEM_ADMIN | Create a user account |
| `PATCH` | `/admin/users/{id}/deactivate` | SYSTEM_ADMIN | Deactivate a user |
| `GET` | `/admin/audit-logs` | SYSTEM_ADMIN, AUDITOR, COMPLIANCE_OFFICER | Full audit trail |
| `GET` | `/admin/audit-logs/applications/{id}` | SYSTEM_ADMIN, AUDITOR, COMPLIANCE_OFFICER | Audit trail for one application |

---

## Application Lifecycle

```
DRAFT → SUBMITTED → UNDER_REVIEW → REVIEW_COMPLETED → APPROVED
                                                      → REJECTED
      → WITHDRAWN (from DRAFT or SUBMITTED)
```

The state machine rejects any transition not listed above and returns `409 Conflict`.

---

## Error Handling

| Situation | HTTP Status |
|-----------|-------------|
| Unauthenticated (no or invalid token) | `401` |
| Authenticated but insufficient role | `403` |
| Resource not found | `404` |
| Validation failure | `400` |
| Illegal state transition | `409` |
| Duplicate registration ID | `409` |

No raw stack traces are returned. All errors follow a consistent structure:
```json
{
  "status": 403,
  "title": "Forbidden",
  "detail": "Only the applicant can perform this action",
  "timestamp": "2026-05-11T10:30:00Z"
}
```

---

## Running Tests

```bash
mvn test
```

Tests use Testcontainers to test with a real PostgreSQL instance
Docker Desktop must be running.

---

## Fresh Start

To wipe all data and reseed:
```bash
docker compose down -v
docker compose up -d
mvn spring-boot:run
```
