# Free Deployment Stack — Render + Vercel + MySQL-Compatible DB

This guide describes the simplest free deployment path for the current RenewSim codebase.

## Why this stack

RenewSim currently runs on **MySQL**, not PostgreSQL.

That means the fastest path to a public demo is:

- **Frontend**: Vercel
- **Backend**: Render
- **Database**: a **MySQL-compatible** free database, for example TiDB Serverless

This avoids forcing an unnecessary migration to PostgreSQL/Supabase while the product is still being packaged for demo and interview use.

## Target architecture

```text
Vercel (frontend)
        |
        v
Render Web Service (Spring Boot backend)
        |
        v
TiDB Serverless / MySQL-compatible database
```

## Backend on Render

### 1. Create the database first

Create a free MySQL-compatible database.

Recommended option:

- TiDB Serverless

You will need:

- JDBC URL
- username
- password

## 2. Create the Render service

Create a new **Web Service** in Render:

- source repo: `RenewSim-backend`
- runtime: Docker
- branch: use a release-ready branch or `main` after the controlled merge
- health check path: `/actuator/health`

Render will build directly from the existing `Dockerfile`.

## 3. Required backend environment variables

Set these in Render:

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=<jdbc-url>
SPRING_DATASOURCE_USERNAME=<db-user>
SPRING_DATASOURCE_PASSWORD=<db-password>

JWT_SECRET_BASE64=<base64-secret>
SECURITY_JWT_ISSUER=renewsim-auth
SECURITY_JWT_AUDIENCE=renewsim-app

OPENWEATHER_API_KEY=<optional-but-recommended>
OPENWEATHER_BASE_URL=https://api.openweathermap.org

APP_FRONTEND_URL=<vercel-url>
APP_EMAIL_VERIFICATION_EXPIRATION_HOURS=48
```

For the first public demo, the backend does **not** need a live email provider to demonstrate the main flow.

Recommended baseline:

- public login via JWT
- register flow available
- stage/showcase verification and reset links emitted through the logging email adapter
- real outbound email deferred until a later production-oriented slice

## Frontend on Vercel

The frontend should live in its own repository and point to the Render backend URL.

Minimum environment variable expected on the frontend side:

```text
VITE_API_BASE_URL=https://<your-render-service>.onrender.com
```

If the frontend relies on cookies or authenticated requests, verify that the backend CORS configuration includes the final Vercel domain.

## Stage / showcase local stack

For a local stage-like environment, this repository already includes:

- `docker-compose.stage.yml`
- `.env.stage.example`

Use it like this:

```bash
cp .env.stage.example .env.stage
# edit .env.stage with real values
docker compose --env-file .env.stage -f docker-compose.stage.yml up --build -d
```

This is useful for validating the deployment shape before publishing a public demo.

`stage` is intentionally suitable for showcase use:

- it can behave like local development for auth/register flows
- it does not require Brevo to demonstrate login/register
- it keeps the public demo focused on JWT auth, simulations and observability

## Smoke test checklist after deploy

After Render is up, validate these endpoints manually:

1. `POST /api/v1/auth/login`
2. `POST /api/v1/simulations`
3. `GET /api/v1/simulations/dashboard`
4. `PUT /api/v1/simulations/{id}`
5. `DELETE /api/v1/simulations/{id}`
6. `GET /actuator/health`
7. `GET /actuator/prometheus`

## Recommended order of execution

1. Merge documentation alignment into `dev/v1.2.0`
2. Validate local stage with `docker-compose.stage.yml`
3. Provision MySQL-compatible free DB
4. Deploy backend to Render
5. Point frontend to Render from Vercel
6. Run smoke tests
7. Only then prepare the controlled merge to `main`

## Notes

- Do not use demo seeders automatically in production.
- Keep stage/showcase seed data separate from production data policy.
- Physical purge of deleted simulations is intentionally separate from the user-facing soft delete flow.

Related issues:

- `#241` free deployment stack
- `#242` stage/showcase demo bootstrap
- `#243` controlled release to `main`
