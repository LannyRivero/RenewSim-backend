# Controlled purge for deleted simulations

Deleted simulations are physically purged only through an explicit operational use case. User-facing delete remains a soft delete that moves the simulation to `DELETED` and keeps normal history/dashboard reads from showing it.

## Decision

| Topic | Decision |
|-------|----------|
| User delete | `DeleteSimulationService` keeps marking simulations as `DELETED`; it does not physically remove rows. |
| Physical purge | `PurgeDeletedSimulationsUseCase` deletes only simulations already in `DELETED` status and older than the retention cutoff. |
| Actor | Operational job or explicit admin maintenance action. No public user endpoint is exposed. |
| Retention | Caller supplies `retentionDays`; it must be greater than zero. Recommended production baseline: 30 days or more. |
| Cutoff source | Application `Clock`, so tests and future jobs can use deterministic time. |

## Data relationship behavior

| Relation | Database behavior during physical purge | Treatment |
|----------|------------------------------------------|-----------|
| `simulation_technologies.simulation_id` | `ON DELETE CASCADE` | Child rows are removed with the simulation. |
| `simulation_share_tokens.simulation_id` | `ON DELETE CASCADE` | Share tokens are removed with the simulation. |
| `chat_sessions.simulation_id` | `ON DELETE SET NULL` | Chat sessions remain as user/AI history but lose the simulation reference. |
| `simulations.scenario_id` | Origin reference, indexed, no FK in current migration | No cascade is involved; scenario rows are not affected. |

## Review checklist

- Soft delete paths still call `Simulation.delete()` and `repository.save(...)`.
- Purge paths call `PurgeDeletedSimulationsUseCase`; they do not reuse user delete endpoints.
- Retention is explicit at call time and validated before persistence.
- Future schedulers/admin controllers must remain protected as operational/admin-only actions.
