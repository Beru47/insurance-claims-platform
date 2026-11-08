# Insurance Claims Platform

Non-life insurance claims backend: declare a claim, score its priority, assign an expert, track its status and compute the indemnity.

[![CI](https://github.com/Beru47/Syst-me-de-gestion-des-sinistres-pour-assurances-non-vie-VERMEG-/actions/workflows/ci.yml/badge.svg)](https://github.com/Beru47/Syst-me-de-gestion-des-sinistres-pour-assurances-non-vie-VERMEG-/actions/workflows/ci.yml)
![Java 17](https://img.shields.io/badge/Java-17-orange)
![Spring Boot 3.1](https://img.shields.io/badge/Spring%20Boot-3.1.5-6DB33F)

## Context

Built in 2025 as my final-year project (PFE) at VERMEG, for the engineering degree in Applied Mathematics at ENSIT. The project scope was a claims management system for non-life insurance covering declaration, tracking, indemnification and fraud detection, designed around an event-driven architecture (Kafka), Kubernetes deployment, Elasticsearch indexing, Keycloak RBAC and Prometheus/Grafana monitoring.

This repository holds the Spring Boot backend. Be aware of what is and is not in it:

| Area | State in this repo |
|---|---|
| Claim declaration, tracking, expert assignment, indemnity, dashboard, JWT auth | Implemented |
| Kafka | Dependency and producer config/publisher classes are present but commented out |
| Fraud detection | `ClaimStatus.SUSPECTED_FRAUD`, model and repository exist; controller, service and event publisher are empty stubs |
| Keycloak | Dependency and a config class present; the active security is the app's own JWT with role checks |
| Elasticsearch, MongoDB | Starters on the classpath, no indexing code in this repo |
| Kubernetes, Prometheus, Grafana | Not part of this repository |

## Features

- **Claim declaration** (`multipart`, with optional media files) tied to a policy, with validation and a generated claim number.
- **Priority scoring**: a logistic score (claim type severity, location risk, description keywords, time-decayed claim history through a gamma CDF, coverage bonus) normalised to 0-100. Score of 80 or more marks the claim `URGENT`, otherwise `PENDING`.
- **Status lifecycle**: `PENDING`, `UNDER_REVIEW`, `SUSPECTED_FRAUD`, `APPROVED`, `REJECTED`, `URGENT`, `IN_PROGRESS`.
- **Expert workflow**: automatic assignment with workload and specialty scoring, assignment preview and analytics, expert reports, meetings, claim approval.
- **Indemnification**: base amount per claim type, severity uplift, adjustment by the client's claim history, payout cap.
- **Roles**: client, expert, manager (gestionnaire), admin, with JWT login and role-based route protection.
- **Dashboard** endpoints and policy / asset management.

## Architecture

```mermaid
flowchart LR
    ui["Angular frontend (CORS origin localhost:4200)"]
    api["Spring Boot API"]
    sec["JWT filter and role checks"]
    svc["Services: claims, experts, indemnity, policies"]
    db[("PostgreSQL")]
    files["Uploaded media (local disk)"]
    kafka["Kafka"]
    es["Elasticsearch"]
    kc["Keycloak"]

    ui --> api
    api --> sec
    sec --> svc
    svc --> db
    svc --> files
    svc -. claim events, planned .-> kafka
    kafka -. indexing, planned .-> es
    sec -. RBAC target .-> kc
```

Solid links exist in this repo, dotted links are the target design from the project scope and are not wired in the code.

## Tech stack

Java 17, Spring Boot 3.1.5 (Web, Data JPA, Security, OAuth2 resource server, Mail, Validation), PostgreSQL, jjwt, Apache Commons Math (gamma distribution), Lombok, JUnit 5 and Mockito, Maven, GitHub Actions. On the classpath but unused so far: Spring Kafka, Spring Data Elasticsearch, Spring Data MongoDB, Keycloak starter, Google Maps client.

## Project structure

```
src/main/java/com/vermeg/sinistpro
  config/      Jackson, CORS, JWT filter, Kafka and Keycloak config
  controller/  REST controllers and request/response DTOs
  service/     Business logic (SinistreService, ExpertService, IndemnificationService, ...)
  model/       JPA entities: Sinistre, Policy, Client, Expert, Report, Meeting, Notification, ...
  repository/  Spring Data JPA repositories
  security/    SecurityConfig, JwtUtil
  event/       Kafka publishers (commented out / stubs)
  exception/   Global exception handler
src/main/resources/application.yml
src/test/java  Unit tests
```

## Run locally

Prerequisites: JDK 17+, Docker (for PostgreSQL).

```bash
cp .env.example .env              # then edit the values
docker compose up -d              # PostgreSQL on localhost:5433
set -a; . ./.env; set +a          # export the variables (Git Bash / Linux / macOS)
./mvnw spring-boot:run            # API on http://localhost:8081
```

Configuration comes from environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (base64), `SINISTRE_SECRET_KEY`. Hibernate is set to `create-drop`, so the schema is rebuilt on each start. The claim upload directory is currently hard-coded to `/uploads/`.

## API overview

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/auth/signup`, `/login`, `/admin-login`, `/expert-login` | Register and authenticate, returns a JWT |
| POST | `/api/sinistres/declare` | Declare a claim (multipart: `request` JSON + `mediaFiles`) |
| GET | `/api/sinistres`, `/api/sinistres/{id}` | List claims, get one |
| PATCH | `/api/sinistres/{id}/status` | Update claim status |
| POST | `/api/policies/{clientId}` | Create a policy for a client |
| GET | `/api/policies/{policyId}` | Get a policy |
| POST | `/api/assets/{policyId}` | Add an asset to a policy |
| POST | `/api/indemnisations/calculate/{sinistreId}` | Compute and store the indemnity |
| POST | `/api/experts/assign/{sinistreId}` | Assign the best-scored expert |
| GET | `/api/experts/preview-assignment/{sinistreId}` | Preview expert scores |
| GET | `/api/experts/{expertId}/claims`, `/workload` | Expert's claims and workload |
| POST | `/api/experts/{expertId}/claims/{sinistreId}/report`, `/meeting`, `/approve` | Report, schedule a meeting, approve |
| GET | `/api/experts/analytics/assignments` | Assignment analytics |
| GET | `/api/dashboard/**`, `/api/admin/**` | Dashboard and admin (admin routes require `ROLE_ADMIN`) |

## Testing

```bash
./mvnw verify
```

Unit tests (JUnit 5, Mockito) cover the indemnity rules: base amount, severity uplift, client-history adjustment, invalid policy and unknown claim. The same command runs in GitHub Actions on every push and pull request. There are no integration tests yet.

## What I would improve next

- Finish the Kafka flow (claim events publisher and consumers) and index claims in Elasticsearch.
- Implement the fraud detection service that the `SUSPECTED_FRAUD` status and the Kafka publisher stub anticipate.
- Delegate authentication to Keycloak instead of the in-app JWT, and remove the permissive `KeycloakConfig` chain.
- Remove commented-out duplicate code left in several classes, and fix mixed French/English naming.
- Replace `create-drop` with Flyway migrations, make the upload directory configurable, add Testcontainers integration tests.
- Add a Dockerfile, Kubernetes manifests and Actuator/Prometheus metrics.
