# Simulation Phase 7 Flows

Phase 7 defines two valid ways to create a simulation and restores the product model that separates `energyType`, `technologyIds`, and `scenarioId`.

## Quick path

1. Use `POST /api/v1/simulations` for manual simulations.
2. Use `POST /api/v1/simulations/from-scenario` to start from a predefined scenario.
3. Persist the simulation with its own snapshot so later catalog changes do not mutate stored results.

## Core model

| Topic | Decision |
|-------|----------|
| Manual input | The current manual endpoint is solar-only and receives full solar simulation inputs. |
| Scenario input | The user provides `scenarioId` plus allowed overrides (`name`, `location`). |
| `energyType` | Remains part of the target product model, but the current manual HTTP contract is still solar-only. |
| `technologyIds` | Persisted as the recommended/comparable technologies for the simulation. |
| `scenarioId` | Optional persisted reference to the source scenario. |
| Snapshot rule | A simulation created from a scenario copies the scenario defaults into its own persisted snapshot at creation time. |

## Flow details

### 1. Manual simulation

- Request enters `CreateSimulationController` through `POST /api/v1/simulations`.
- `CreateSimulationWebMapper` builds `CreateRealSimulationCommand` for solar simulations and hard-codes `Technology.solar()`.
- `CreateSimulationService` validates the active technology, recommends `technologyIds` when the request does not provide them, persists the draft, runs the engine, and persists the completed simulation.

### 2. Simulation from scenario

- Request enters `CreateSimulationController` through `POST /api/v1/simulations/from-scenario`.
- `CreateSimulationFromScenarioService` resolves the active scenario through `ScenarioLookupPort`.
- The service derives `energyType` from the scenario's `technologyId` through `TechnologyLookupPort`.
- The service copies scenario defaults into a new `CreateRealSimulationCommand`:
  - system capacity
  - investment amount/currency
  - tariff
  - annual consumption
- The service persists `scenarioId` and recommended `technologyIds` together with the simulation snapshot.

## What this phase protects

- `technologyId` from the scenario does not replace the simulation's `technologyIds` list.
- A later scenario catalog update does not rewrite historical simulations.
- Cleanup of legacy snapshot compatibility happens only after this functional model is in place.

## Verification checklist

- [ ] Manual creation remains solar-only until a broader manual contract is introduced.
- [ ] Scenario creation persists `scenarioId`.
- [ ] Simulations persist `technologyIds` as recommended/comparable technologies.
- [ ] Stored simulations remain stable even if scenario defaults change later.

## Relevant files

- `src/main/java/com/renewsim/backend/simulation_service/create/application/CreateSimulationService.java`
- `src/main/java/com/renewsim/backend/simulation_service/create/application/CreateSimulationFromScenarioService.java`
- `src/main/java/com/renewsim/backend/simulation_service/domain/model/Simulation.java`
- `src/main/java/com/renewsim/backend/simulation_service/infrastructure/adapter/out/persistence/SimulationRecordEntityMapper.java`
- `src/main/resources/db/migration/V26__add_simulation_scenario_origin.sql`
