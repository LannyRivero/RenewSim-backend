# Simulation assessment and location guidance contract

This contract keeps the result of the modeled simulation separate from cross-technology guidance based on location and demand. It defines a future response shape; issue #265 does not add a provider, calculate recommendations, or change the current HTTP response.

## Contract at a glance

| Section | Meaning | May be ranked? |
|---|---|---|
| `simulationAssessment` | Result for the technology actually modeled by this simulation. Its metrics and existing assessment outcome retain their current meaning. | No new cross-technology rank. |
| `locationGuidance` | A set of catalog technologies recommended **to this simulation**, evaluated against its location, demand, and relevant external resource evidence for each technology's energy type. Each entry is associated with this simulation revision. | Only when evidence is sufficiently reliable and comparable. |

The existing simulation-detail response remains unchanged in this issue. The sections below are the target contract for follow-on implementation, not fields currently returned by the API.

## Target response shape

```json
{
  "simulationAssessment": {
    "status": "assessed",
    "technology": {
      "energyType": "solar",
      "catalogTechnologyId": 42,
      "catalogTechnologyName": "Monocrystalline PV"
    },
    "assessmentOutcome": "viable_with_reservations",
    "reasons": [
      { "code": "RESOURCE_MODELED", "message": "Assessment uses the modeled solar resource." }
    ],
    "confidence": { "value": 0.92, "basis": "modeled_resource_and_input_quality" },
    "evidence": [
      { "source": "PVGIS", "dataset": "2005-2020", "observedAt": "2026-10-21T12:00:00Z" }
    ],
    "limitations": []
  },
  "locationGuidance": {
    "status": "available",
    "simulationId": "sim-123",
    "simulationRevision": 3,
    "context": {
      "location": "Sevilla, ES",
      "simulatedEnergyType": "solar",
      "annualDemandKwh": 120000
    },
    "recommendations": [
      {
        "target": {
          "kind": "catalog_technology",
          "energyType": "solar",
          "catalogTechnologyId": 42,
          "catalogTechnologyName": "Monocrystalline PV"
        },
        "status": "recommended",
        "rank": 1,
        "assessment": {
          "estimatedAnnualEnergyKwh": 128000,
          "capacityFactorPct": 22.4,
          "efficiencyPct": 19.8,
          "estimatedAnnualCo2ReductionTons": 56.2
        },
        "reasons": [{ "code": "RESOURCE_MATCH", "message": "Local evidence supports this option." }],
        "confidence": { "value": 0.86, "basis": "resource_coverage_and_quality" },
        "evidence": [
          { "source": "PVGIS", "dataset": "2005-2020", "observedAt": "2026-10-21T12:00:00Z" }
        ],
        "limitations": []
      }
    ],
    "limitations": []
  }
}
```

Each item in `locationGuidance.recommendations` means: **recommend this target catalog technology to this simulation**, considering the simulation's energy type, location, demand, and resource evidence relevant to the target technology's energy type. It is not a stand-alone recommendation detached from a simulation, nor a record of a technology already used by the simulation. The enclosing `simulationId` and `simulationRevision` identify the simulation receiving the recommendations; in a simulation-detail response, `simulationId` may be represented by the existing root `id` instead of duplicated.

`catalogTechnologyId` and `catalogTechnologyName` are required when `target.kind` is `catalog_technology`; they are omitted for `energy_type`. A response may contain multiple recommendations with the same energy type when they refer to different active catalog entries. Catalog membership alone is not evidence of local suitability.

`simulationAssessment.assessmentOutcome` describes the assessment of the technology that was actually simulated. It is not a recommended technology. In particular, do not reinterpret the existing `summary.recommendation` field as a location-guidance recommendation or replace it with one.

### Effect of choosing a recommendation

Selecting a recommended technology does not directly overwrite the current `simulationAssessment` or its existing recommendation card. If the user chooses that technology and runs/updates the simulation with its applicable inputs, the system must calculate a new simulation result for that selected technology. Its generation, demand fit, investment, operating costs, savings, payback, and other supported metrics may differ; the new revision's `simulationAssessment` and recommendation card are then derived from those recalculated results. Keep the prior successful revision unchanged and browsable. A guidance candidate is not itself a completed simulation result, and must not be presented as one.

For the current solar simulation flow, the assessment outcome is derived from that run's financial results (including NPV, IRR, and payback) and specific yield. This is the kind of downstream result that can change after choosing a different technology and recalculating; location guidance alone must not change it.

## Status and ranking rules

### Guidance status

| Status | Meaning | Recommendations |
|---|---|---|
| `available` | Guidance was assessed using sufficient evidence. | May include assessed and unassessed candidates. |
| `not_assessed` | The request lacks required context or relevant evidence is missing/insufficient. | Empty or explicitly `not_assessed`; never present an unsupported rank. |
| `unavailable` | A guidance-only provider failed. | Empty; do not return stale recommendations. The core simulation remains successful. |

Candidate statuses are `recommended`, `viable_with_reservations`, `not_recommended`, and `not_assessed`. `rank` is a positive integer only for candidates eligible for comparison; it is `null`/omitted for `not_assessed` and any candidate without comparable evidence. The ranking policy and its version must be identifiable in provenance when ranking is implemented. The contract intentionally sets no numeric evidence threshold; product/domain policy must define that threshold before ranking is enabled.

Evaluate each candidate using evidence relevant to its own energy type at the simulation's location, then assess how that option fits the simulation's demand and modeled energy context. For example, solar irradiance/temperature data can support a solar candidate; it does not establish wind or hydro resource suitability. Reuse resource data already fetched for the simulation only when it is relevant to that candidate and has adequate provenance/quality. Otherwise obtain evidence from an appropriate provider in the follow-on implementation.

### Candidate technology factors

Where the required inputs and comparable definitions are available, assess a catalog candidate using its installed/rated power or supported capacity range, capacity factor, efficiency, estimated energy production at the simulation location, fit to the simulation's demand and modeled energy context, and estimated CO₂ reduction. Other catalog and environmental/economic attributes may be considered when relevant and supported by evidence. These values inform the decision; the contract does not prescribe a score, weights, or ordering among them.

Distinguish catalog characteristics from location-specific estimates. For example, a catalog CO₂ reduction factor is not itself the candidate's estimated annual CO₂ reduction: the latter needs an explicit production estimate, baseline/emissions assumptions, unit, and provenance. Do not fabricate candidate energy output by applying the simulated technology's resource profile to a different energy type. If a material factor cannot be estimated reliably, expose that limitation and do not use it to justify a rank.

Missing location, resource data, or demand needed for a particular assessment must yield `not_assessed` with a safe explanation in `limitations`, not an inferred negative recommendation. Do not recommend a catalog technology solely because it exists or because it was selected for the simulation. Demand may inform suitability; source-specific economics may be included only when valid and available for that source. Do not compare economic metrics across energy types when their assumptions or definitions are not comparable.

Confidence is an optional normalized value from `0` to `1`, accompanied by a short basis. It describes evidence confidence, not a probability of financial success. Reasons and limitations are explanatory, not a replacement for machine-readable status or evidence provenance.

## Catalog identity, lifecycle, and provenance

- Guidance recommends a specific active catalog technology to a simulation. Capture its energy type and catalog identity. A generic energy-type candidate may be used only when no specific catalog technology is being recommended; never use catalog metadata alone to claim resource suitability.
- Do not place recommendations in `simulation_technologies`; that association records technologies used/selected for the simulation, not guidance candidates.
- A successful execution's guidance must be attributable to its simulation revision, input snapshot, catalog identity/name, evidence sources, and algorithm/provider version. Preserve those snapshots when catalog entries or providers change.
- Inactive catalog entries are not new candidates. Previously persisted results remain readable from their snapshots.
- Each recommendation belongs to one simulation revision; one revision may recommend multiple technologies, and a technology may be recommended to multiple revisions. Prevent duplicate entries for one technology within one revision unless product rules explicitly permit them.
- Provider failure for guidance alone must not fail the core simulation/update. Persist the successful revision with `locationGuidance.status = unavailable`, a safe limitation, and no stale candidates. This does not alter failure policy for providers required by the core simulation.

These lifecycle and persistence statements constrain follow-on implementation; this contract issue introduces no schema, persistence, or provider changes.

## Compatibility and rollout

Issue #265 does not rename, remove, or reinterpret existing detail-response fields, including the current solar simulation summary/recommendation. Follow-on implementation may add the two top-level sections only after the API contract and consumers are reviewed. Treat fields as optional for older stored revisions and older clients; do not synthesize historical location guidance from current catalog or provider data. If consumers reject unknown JSON fields or the response must change incompatibly, version the endpoint rather than silently changing existing semantics.

## Acceptance checklist

- [x] Simulation assessment and location guidance have separate meanings.
- [x] Energy-type and catalog-technology targets, including multiple catalog technologies per energy type, are distinguishable.
- [x] Ranking requires comparable evidence; `not_assessed` has no rank.
- [x] Location, demand, economics, reasons, confidence, evidence, and limitations have explicit rules.
- [x] Missing data and guidance-provider failure have defined behavior.
- [x] Existing response compatibility and implementation scope are documented.
