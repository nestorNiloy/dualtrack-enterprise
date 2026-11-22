# DualTrack Enterprise

A secure, multi-user application management system built with Java 17, Spring Boot, Spring Security, PostgreSQL, JPA/Hibernate, Flyway, vanilla JavaScript, Docker and GitHub Actions.

> This is a parallel development repository. The original `dual-study-application-tracker` repository and its live deployment are intentionally untouched.

## Current implementation
- Registration, login and logout using server-side HTTP sessions and BCrypt password hashing.
- Same-origin frontend served by Spring Boot; CSRF protection uses the Spring Security cookie token repository.
- Per-user application ownership enforced in the service/repository layer.
- Application CRUD with DTO validation, pagination, status filtering, search, dashboard statistics and status history.
- PostgreSQL schema managed by Flyway; Hibernate validates rather than mutates schema.
- Docker Compose local environment and GitHub Actions Maven CI.

## Important current limitation
The existing dashboard HTML has been copied into Spring Boot static resources, but its JavaScript still expects the old unauthenticated array API. The backend is implemented as the secure API foundation; the frontend login/session integration and updated paginated response rendering are the next required implementation task before calling the application end-to-end complete.

## Beginner setup

Follow [`START_HERE.md`](START_HERE.md) for Windows installation, local launch, and new GitHub repository instructions.

## Requirements
- Java 17+
- Maven 3.9+
- Docker Desktop (recommended for PostgreSQL)
- Git

## Run locally with Docker
```bash
cd backend
docker compose up --build
```
Open http://localhost:8080. Stop with `Ctrl+C`, then `docker compose down` (database data remains in the named volume). To delete local database data too, use `docker compose down -v`.

## Run without Docker
Start a PostgreSQL 16 database, then from `backend/`:
```bash
mvn clean verify
mvn spring-boot:run
```
Set `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`; defaults target a local database named `dualtrack`.

## API overview
- `POST /api/auth/register` — `{ "email": "you@example.com", "password": "at-least-10-characters" }`
- `POST /api/auth/login` — same fields; creates an HTTP session
- `POST /api/auth/logout` — invalidates session (CSRF token required)
- `GET /api/auth/me` — current user
- `GET /api/applications?page=0&size=20&status=APPLIED&q=SAP`
- `POST /api/applications`, `PUT /api/applications/{id}`, `DELETE /api/applications/{id}`
- `GET /api/applications/stats`
- `GET /api/applications/{id}/history`
- `GET /actuator/health`

All application operations are scoped to the authenticated user. A guessed ID belonging to another account returns 404.

## Deployment notes
Deploy the Docker image as one web service and attach a managed PostgreSQL database. Set `DATABASE_URL` in JDBC form (`jdbc:postgresql://...`), `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `COOKIE_SECURE=true`. Keep frontend and API on the same origin. Never commit production credentials. Back up the old production database separately; this parallel project does not migrate or alter it.

## Verification status
The source scaffold was generated in this workspace. Maven is not installed in the generation environment, so compilation and tests have **not** been run here. Run `mvn clean verify` locally or let GitHub Actions verify the pushed repository. Do not describe it as production-ready until CI passes and the frontend auth integration is completed.
