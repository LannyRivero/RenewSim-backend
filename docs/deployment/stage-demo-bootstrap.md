# Stage Demo Bootstrap Data

Local/docker/stage/showcase can start with demo users, technologies, scenarios, and completed simulations so the UI has visible dashboard, history, and detail data immediately.

## Quick Path

1. Run the backend with the `local`, `docker`, `stage`, or `showcase` Spring profile.
2. Keep `DEMO_BOOTSTRAP_ENABLED=true` or omit it in `local`/`docker`/`stage`, where it defaults to `true`.
3. Set `DEMO_BOOTSTRAP_PASSWORD` if you do not want the default demo password.
4. Start the backend; the bootstrap runs once per missing record and does not duplicate demo data.

## Demo Accounts

| Username | Email | Role | Password |
| --- | --- | --- | --- |
| `demo.admin` | `demo.admin@renewsim.local` | `ADMIN`, `USER` | `${DEMO_BOOTSTRAP_PASSWORD}` or `DemoPass123!` |
| `demo.user` | `demo.user@renewsim.local` | `USER` | `${DEMO_BOOTSTRAP_PASSWORD}` or `DemoPass123!` |

Use these accounts only for local/docker/stage/showcase demos. Do not enable this bootstrap for real production data.

## Render Variables

| Variable | Stage value | Purpose |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `stage` | Loads stage config and allows demo bootstrap. |
| `DEMO_BOOTSTRAP_ENABLED` | `true` | Explicitly enables demo data creation. |
| `DEMO_BOOTSTRAP_PASSWORD` | custom demo password | Password assigned to both demo accounts. |

`application-local.yml`, `application-docker.yml`, and `application-stage.yml` default `DEMO_BOOTSTRAP_ENABLED` to `true` so local Docker and public stage environments are demo-ready. Set it to `false` to disable seeding without changing code.

## What It Creates

- Roles required by the demo accounts if missing.
- `demo.admin` and `demo.user` as active, email-verified users.
- Three demo technologies: solar, wind, and hydro.
- Three demo scenarios linked to those technologies.
- Solar-only completed simulations split across the demo users, covering `recommended`, `viable_with_reservations`, and `not_recommended` cases for dashboard/history/detail screens.

## Idempotency

The bootstrap is safe to run repeatedly:

- Users are matched by username or email.
- Technologies and scenarios are matched by demo names.
- Simulations are matched by owner and name when not deleted.
- Existing records are reused instead of duplicated.

## Reset Demo Data

For a stage database reset, either recreate the database or remove the demo records and restart the backend with bootstrap enabled.

```sql
DELETE FROM simulation_technologies
WHERE simulation_id IN (
  SELECT id FROM simulations
  WHERE created_by IN ('demo.admin', 'demo.user')
);

DELETE FROM simulations
WHERE created_by IN ('demo.admin', 'demo.user');

DELETE FROM user_roles
WHERE user_id IN (
  SELECT id FROM users
  WHERE username IN ('demo.admin', 'demo.user')
);

DELETE FROM users
WHERE username IN ('demo.admin', 'demo.user');
```

Technologies and scenarios can usually remain because they are harmless showcase catalog data. Remove them manually only if you need a fully clean stage catalog.
