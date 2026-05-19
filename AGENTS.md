# AGENTS.md — 柠檬网

## Repo structure

- `frontend/` — Vue 3 + Vite (plain JS, no TypeScript)
- `backend/` — Spring Boot 3 + Maven + Java 17 + MyBatis Plus
- `docs/` — api.md, database.md, deploy.md, development.md, architecture.md
- No workspace tool ties them together; each is independent

## Essential commands

**Frontend** (from `frontend/`):
- `npm run dev` — dev server on port 5174
- `npm run build` — production build
- No test/lint/formatter configured

**Backend** (from `backend/`):
- `mvn spring-boot:run` — dev server on port 8092
- `mvn clean package -DskipTests` — build JAR
- `mvn test` — run tests

## Architecture quirks

- Single Vue SPA serves both user-facing (`/`, `/resource/:id`, etc.) and admin routes (`/admin/*`). Layout/permission split via `router/index.js` using `meta.requiresAuth` and `meta.requiresAdmin`.
- Vite proxy forwards `/api/*` and `/uploads/*` → `localhost:8092` (configured in `vite.config.js`).
- CORS in `CorsConfig.java` is hardcoded to `http://localhost:5174`.
- JWT tokens stored in `localStorage` (see `utils/auth.js`).
- SQL auto-initialization on dev startup: `schema.sql` + `data.sql` (drop-and-recreate tables each restart).
- `@` path alias maps to `frontend/src/`.
- Frontend uses Vditor (Markdown editor) in split-view mode.
- No Docker, no CI/CD, no containerization.

## Defaults (dev)

| Service | Port | Credentials |
|---------|------|-------------|
| Frontend | 5174 | — |
| Backend | 8092 | — |
| MySQL | 3306 | root / 123456 |
| Admin user | — | admin / admin123 |

If ports conflict, change: `vite.config.js` (frontend), `application-dev.yml` (backend port + DB), `CorsConfig.java` (CORS origin).

## Schema note

`schema.sql` defines 5 tables (no `comment` table). `docs/database.md` documents 6 tables including `comment`. The comment feature may be partially implemented.

## Docs

- `docs/api.md` — full REST API reference (JWT Bearer auth, unified response `{code, message, data}`)
- `docs/development.md` — coding conventions (Git commit format, Java/Vue naming, etc.)
- `docs/deploy.md` — production deployment
