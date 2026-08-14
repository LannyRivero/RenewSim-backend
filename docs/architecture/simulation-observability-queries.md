# Simulation Observability Queries

This guide turns the `simulation_service` metrics already exposed through `/actuator/prometheus` into concrete Prometheus queries that are useful for demos, troubleshooting, and interview walkthroughs.

## Quick path

1. Use the throughput queries to explain how often `create`, `detail`, `dashboard`, and `history` run.
2. Use the duration queries to show which use cases or providers are getting slower.
3. Use the degradation and provider outcome queries to explain when snapshots degrade or when external dependencies fall back/fail.

## Metric map

| Metric | What it answers |
|-------|------------------|
| `simulation_service_use_case_total` | How many times each core use case succeeded, failed, or degraded |
| `simulation_service_use_case_duration_seconds_*` | How long each core use case takes |
| `simulation_service_snapshot_degraded_total` | How often detail degraded because the stored result snapshot was invalid |
| `simulation_service_provider_calls_total` | How OpenWeather/PVGIS behave: success, fallback, or error |
| `simulation_service_provider_call_duration_seconds_*` | How long each provider call takes |

## Available labels

| Metric family | Labels |
|--------------|--------|
| `simulation_service_use_case_*` | `use_case`, `outcome` |
| `simulation_service_snapshot_degraded_total` | `reason` |
| `simulation_service_provider_*` | `provider`, `outcome` |

## Use-case throughput

### Requests per minute by use case

```promql
sum by (use_case, outcome) (
  rate(simulation_service_use_case_total[5m]) * 60
)
```

Use this to show whether `create`, `detail`, `dashboard`, or `history` is the busiest path and whether failures/degradations are growing.

### Total executions by use case and outcome

```promql
sum by (use_case, outcome) (
  simulation_service_use_case_total
)
```

Use this for a simple cumulative demo when you do not need a time window.

## Use-case latency

### Average latency by use case

```promql
sum by (use_case, outcome) (
  rate(simulation_service_use_case_duration_seconds_sum[5m])
)
/
sum by (use_case, outcome) (
  rate(simulation_service_use_case_duration_seconds_count[5m])
)
```

This is the easiest “how fast is each flow?” query to explain live.

### 95th percentile latency by use case

```promql
histogram_quantile(
  0.95,
  sum by (le, use_case, outcome) (
    rate(simulation_service_use_case_duration_seconds_bucket[5m])
  )
)
```

Use this only if histogram buckets are available in the exported metric set.

## Snapshot degradation

### Snapshot degradation count

```promql
sum by (reason) (
  simulation_service_snapshot_degraded_total
)
```

This is the cleanest query to explain historical snapshot quality problems.

> Note: in the current implementation, a degraded detail read also finishes as a `SimulationNotFoundException`, so `detail` degradation can increment both `simulation_service_snapshot_degraded_total` and the generic `simulation_service_use_case_total{use_case="detail", outcome="error"}` series. Treat the snapshot degradation metric as the precise source of truth for invalid historical snapshots.

### Snapshot degradation rate per minute

```promql
sum by (reason) (
  rate(simulation_service_snapshot_degraded_total[5m]) * 60
)
```

Use this if you want to explain whether degradation is active or only historical.

## Provider outcomes

### Provider calls per minute

```promql
sum by (provider, outcome) (
  rate(simulation_service_provider_calls_total[5m]) * 60
)
```

Use this to explain:

- `openweather` success vs fallback
- `pvgis` success vs error

### Provider outcome totals

```promql
sum by (provider, outcome) (
  simulation_service_provider_calls_total
)
```

Good for cumulative demo views.

## Provider latency

### Average provider latency

```promql
sum by (provider, outcome) (
  rate(simulation_service_provider_call_duration_seconds_sum[5m])
)
/
sum by (provider, outcome) (
  rate(simulation_service_provider_call_duration_seconds_count[5m])
)
```

This is the main query to explain whether PVGIS or OpenWeather is slower.

### 95th percentile provider latency

```promql
histogram_quantile(
  0.95,
  sum by (le, provider, outcome) (
    rate(simulation_service_provider_call_duration_seconds_bucket[5m])
  )
)
```

Again, use this only if histogram buckets are present.

## Demo reading guide

### 1. Show use-case health first

Start with:

```promql
sum by (use_case, outcome) (simulation_service_use_case_total)
```

Explain that the module already distinguishes `success`, `error`, and `degraded` outcomes per core flow.

### 2. Show degradation separately

Then show:

```promql
sum by (reason) (simulation_service_snapshot_degraded_total)
```

Explain that detail degradation is not mixed with generic failures. Invalid historical snapshots are tracked explicitly.

### 3. Show provider behavior

Then show:

```promql
sum by (provider, outcome) (simulation_service_provider_calls_total)
```

Explain the design choice:

- OpenWeather can degrade to `fallback`
- PVGIS should report `error`, not synthetic fallback data

### 4. Show latency last

Finish with average latency queries for use cases and providers if you want to talk about performance.

## What these queries protect

- Use-case outcomes stay low-cardinality and explainable.
- Snapshot degradation is visible as its own technical signal.
- Provider behavior lines up with the resilience model already implemented in `simulation_service`.
- No query depends on user IDs, simulation IDs, coordinates, or exception messages.

## Verification checklist

- [ ] `simulation_service_use_case_total` appears in `/actuator/prometheus`
- [ ] `simulation_service_use_case_duration` appears in `/actuator/prometheus`
- [ ] `simulation_service_snapshot_degraded_total` appears in `/actuator/prometheus`
- [ ] `simulation_service_provider_calls_total` appears in `/actuator/prometheus`
- [ ] `simulation_service_provider_call_duration` appears in `/actuator/prometheus`
- [ ] The only labels relied on here are `use_case`, `outcome`, `provider`, and `reason`

## Relevant files

- `src/main/java/com/renewsim/backend/simulation_service/shared/application/SimulationUseCaseTelemetry.java`
- `src/main/java/com/renewsim/backend/simulation_service/shared/application/SimulationProviderTelemetry.java`
- `src/main/java/com/renewsim/backend/simulation_service/create/application/CreateSimulationService.java`
- `src/main/java/com/renewsim/backend/simulation_service/detail/application/GetSimulationService.java`
- `src/main/java/com/renewsim/backend/simulation_service/dashboard/application/GetPortfolioDashboardService.java`
- `src/main/java/com/renewsim/backend/simulation_service/history/application/ListSimulationsService.java`
- `src/main/java/com/renewsim/backend/simulation_service/infrastructure/adapter/out/external/OpenWeatherMapAdapter.java`
- `src/main/java/com/renewsim/backend/simulation_service/infrastructure/adapter/out/external/PvgisSolarResourceAdapter.java`
