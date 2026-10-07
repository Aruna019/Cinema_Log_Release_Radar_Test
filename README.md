# Cinema Log & Release Radar

A personal movie diary in a hand-drawn notebook style. Users discover films (synced from TMDB), log what they watched with ratings and reviews, see their diary as a scrapbook calendar, keep a watchlist, likes and collections, and set release reminders that arrive in-app or by email.
Built for CP353002 Principles of Software Design and Development with Spring Boot (layered architecture, SOLID, GoF patterns), PostgreSQL and Thymeleaf.

## Group members (Section 03)

| # | Name | Student ID | Section | Branch | Responsibilities |
|---|---|---|---|---|---|
| 1 | นางสาวนันทพร ลุนทอง (Nunthaporn) | 673380409-0 | 03 | `Nunthaporn_673380409-0_03` | Diary (CRUD), watchlist, likes, collections (CRUD), reviews, library stats; diary calendar, library and collection pages; ER diagram & data dictionary |
| 2 | นางสาวปรายฝน ฮกเซ็ง (Prayfon) | 673380591-5 | 03 | `Prayfon_673380591-5_03` | Movie catalog, TMDB integration (Adapter), search & filters with pagination (Builder), discovery shelves and recommendations, sync job; Films / catalog / movie pages and design system |
| 3 | นางสาวปวริศา สีดาชมภู (Pawarisa) | 673380592-3 | 03 | `Pawarisa_673380592-3_03` | Project setup, security & auth, profiles, release reminders (State), notifications (Strategy, Template Method, Factory, Observer), error handling, layout & nav, Docker / CI / deployment |

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security, Mail, Actuator) — generated with Spring Initializr |
| Database | PostgreSQL 16, Flyway migrations |
| Frontend | Thymeleaf templates + vanilla ES modules (no build step) |
| API docs | springdoc-openapi → `/swagger-ui.html` |
| External API | TMDB v3 |
| Tests | JUnit 5, Mockito, Spring Boot Test (`@WebMvcTest`), JaCoCo |
| Delivery | Docker, docker-compose, GitHub Actions, Render + Supabase (PostgreSQL) |

## System architecture

```
Browser (Thymeleaf page + JS modules)
   │  GET /films, /diary …            │  fetch /api/v1/…
   ▼                                   ▼
controller.web  (@Controller)     controller.api (@RestController)   ← Presentation
   └───────────────┬───────────────────┘
                   ▼
service (interfaces) → service.impl (@Transactional)                 ← Business logic
   │  service.notification (Strategy / Template / Factory / Observer)
   │  service.external.tmdb (Adapter)
   ▼
repository (Spring Data JPA, Specification builder)                  ← Data access
   ▼
domain.entity / enums / event  +  dto + mapper                       ← Domain & contracts
   ▼
PostgreSQL (Flyway)
```

Controllers never call repositories. Details: [`doc/diagrams/03-class-diagram.md`](doc/diagrams/03-class-diagram.md), [`doc/diagrams/07-component-deployment.md`](doc/diagrams/07-component-deployment.md), [`doc/design-patterns.md`](doc/design-patterns.md), [`doc/solid-analysis.md`](doc/solid-analysis.md).

## Database design (ER diagram)

13 tables. One-to-One: `users`–`user_profiles`. One-to-Many: `users`→`watched_movies`, `collections`, `reminders`, `notifications`. Many-to-Many: `movies`–`genres`, `user_profiles`–`genres`, `collections`–`movies` (through `collection_movies` with `added_at`).
Diagram: [`doc/diagrams/06-er-diagram.md`](doc/diagrams/06-er-diagram.md) · columns, keys, indexes, cascades: [`doc/data-dictionary.md`](doc/data-dictionary.md).

## Installation & setup

Requirements: JDK 17+, Maven 3.9+ (or IntelliJ IDEA's built-in Maven), PostgreSQL 16 **or** Docker Desktop, a TMDB API key.

1. Create the local database (skip if you use docker-compose):
   ```sql
   CREATE USER cinemalog WITH PASSWORD 'cinemalog';
   CREATE DATABASE cinemalog OWNER cinemalog;
   ```
   Flyway creates every table and the demo data on first start.
2. Set environment variables (copy `.env.example`; never commit real keys):
   ```
   TMDB_API_KEY=your-key            # v3 API key or v4 read token
   DB_URL=jdbc:postgresql://localhost:5432/cinemalog
   DB_USERNAME=cinemalog
   DB_PASSWORD=cinemalog
   ```
   Without `TMDB_API_KEY` the app still runs on the 39 seeded movies.

## How to run

```bash
# option A: everything in Docker
docker compose up --build

# option B: local PostgreSQL
cd code
mvn spring-boot:run
```
Open http://localhost:8080 and log in with **demo@cinemalog.app / cinema123** (or create an account).

## API documentation

Swagger UI: `/swagger-ui.html` (log in through `/login` first; Swagger reuses the session). OpenAPI JSON: `/v3/api-docs`.

| Resource | Endpoints |
|---|---|
| Auth | `POST /api/v1/auth/register` |
| Profile | `GET, PUT /api/v1/users/me` · `PUT /api/v1/users/me/favorite-genres` |
| Movies | `GET /api/v1/movies` (search, filters, **pagination & sorting**) · `GET /{id}` · `/{id}/similar` · `/now-showing` · `/upcoming` · `/top-rated` · `/recommended` · `GET /api/v1/genres` |
| Reviews | `GET /api/v1/movies/{movieId}/reviews` |
| **Diary (CRUD)** | `GET /api/v1/users/me/diary` (paged, `sort=`) · `/calendar?month=YYYY-MM` · `/day?date=` · `GET/PUT/DELETE /{id}` · `POST` (201 + Location) |
| **Collections (CRUD)** | `GET, POST /api/v1/users/me/collections` · `GET, PUT, DELETE /{id}` · `PUT, DELETE /{id}/movies/{movieId}` |
| Watchlist / Likes | `GET /api/v1/users/me/watchlist` · `PUT, DELETE /{movieId}` (same for `/likes`) |
| Reminders | `GET /api/v1/users/me/reminders` · `PUT, DELETE /{movieId}` |
| Notifications | `GET /api/v1/users/me/notifications` · `/unread-count` · `POST /read-all` |
| Library | `GET /api/v1/users/me/library` · `GET /api/v1/users/me/stats` |

Errors always use one JSON shape (`GlobalExceptionHandler`):
```json
{ "timestamp": "…", "status": 400, "error": "Bad Request", "message": "Some fields are not valid.",
  "path": "/api/v1/users/me/collections", "fieldErrors": [ { "field": "name", "message": "Give your collection a name" } ] }
```
Status codes used: 200, 201, 204, 400, 401, 404, 409, 500, 503.

## How to run tests

```bash
cd code
mvn verify                              # runs all tests in ../test + JaCoCo coverage
mvn surefire-report:report-only         # HTML report
```
Reports: `code/target/reports/surefire.html` (tests), `code/target/site/jacoco/index.html` (coverage). Copy them to `test/reports/` before submission.

## Deployment URL

- App: `https://<your-service>.onrender.com` ← fill in after deploying
- Swagger: `https://<your-service>.onrender.com/swagger-ui.html`

Render: New → Web Service → this repo → Docker (uses the root `Dockerfile`). Set `DB_URL` (Supabase → Connect → Session pooler, port 5432: `jdbc:postgresql://<host>.pooler.supabase.com:5432/postgres?sslmode=require`), `DB_USERNAME` (`postgres.<project-ref>`), `DB_PASSWORD`, `TMDB_API_KEY`. Add the Render deploy hook as the GitHub secret `RENDER_DEPLOY_HOOK` to deploy automatically after CI passes on `main`.

## Project structure

```
├── code/                      Spring Boot application
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/cinemalog/
│       │   ├── config/        Security, OpenAPI, Clock, TMDB client
│       │   ├── controller/api REST controllers (/api/v1)
│       │   ├── controller/web Thymeleaf page controllers
│       │   ├── service/       interfaces, impl/, notification/, external/tmdb/, job/
│       │   ├── repository/    Spring Data JPA + specification/
│       │   ├── domain/        entity/, enums/, event/, model/
│       │   ├── dto/           request/, response/, external/tmdb/
│       │   ├── mapper/        entity → DTO
│       │   ├── exception/     ApiException family + GlobalExceptionHandler
│       │   ├── security/      login principal, onboarding redirect
│       │   └── common/        CurrentUserProvider
│       └── resources/
│           ├── db/migration/  Flyway V1–V7
│           ├── templates/     Thymeleaf views
│           └── static/        css/, js/ (core, api, components, pages), img/
├── test/                      JUnit 5 + Mockito tests (Maven reads them from here)
├── doc/                       solid-analysis.md, design-patterns.md, data-dictionary.md, diagrams/, slide/
├── img/                       logo and mascot SVGs
├── Dockerfile · docker-compose.yml · .github/workflows/ci.yml
```

This product uses the TMDB API but is not endorsed or certified by TMDB.
