# Simulation Distributed Tracing Evaluation

Distributed tracing is not worth introducing in `simulation_service` right now.

The module already has the observability layer that solves today's actual problems: correlation id propagation, per-request MDC context, operational logs, provider/use-case metrics, provider health indicators, and Prometheus-ready queries. Adding Micrometer Tracing plus an exporter now would increase moving parts and cognitive load before there is a concrete diagnostic gap that logs and metrics cannot cover.

## Quick path

1. Keep the current observability stack for `simulation_service` as the default operating model.
2. Do not add tracing dependencies or exporters in this slice.
3. Revisit tracing only if provider chains, async flows, or cross-service calls become materially harder to diagnose.

## Decision

| Topic | Decision |
|-------|----------|
| Introduce distributed tracing now | No |
| Reason | Current complexity does not justify Micrometer Tracing + exporter + sampling + backend maintenance |
| Current observability baseline | Sufficient for this bounded context today |
| Future slice if needed | Small tracing spike around `simulation_service` controllers and external providers |

## Current state

`simulation_service` already exposes useful operational signals:

- Prometheus metrics for use cases, providers, degradation, and business counters
- Actuator indicators for bounded-context health and external dependency configuration readiness
- Correlation id propagation through `X-Correlation-Id`
- MDC-enriched logs with request and user context
- Explicit provider-level logging in the simulation flows

Relevant evidence in the repo:

- `pom.xml` includes `spring-boot-starter-actuator`, `micrometer-registry-prometheus`, and `resilience4j-micrometer`
- `src/main/java/com/renewsim/backend/shared/observability/CorrelationIdFilter.java` propagates `X-Correlation-Id`
- `src/main/java/com/renewsim/backend/shared/observability/LoggingMDCFilter.java` enriches MDC per request
- `src/main/resources/logback-spring.xml` emits MDC-backed log fields including `traceId`
- `docs/architecture/simulation-observability-queries.md` documents the metric families already available for `simulation_service`

## Why tracing does not pay off yet

### 1. The module is not a distributed workflow yet

`simulation_service` is still mostly a single Spring Boot process coordinating:

- inbound HTTP controllers
- local use-case orchestration
- two outbound provider calls via `RestTemplate`

That is operationally interesting, but it is not yet a broad service mesh or event-driven chain where span trees would unlock a step change in diagnosis.

Also, the current actuator indicators are not remote reachability probes. `PvgisSimulationHealthIndicator` validates URI configuration, and `OpenWeatherSimulationHealthIndicator` validates configuration plus required wiring. They are useful readiness signals, but they should not be interpreted as proof that PVGIS or OpenWeather are reachable at runtime.

### 2. The current questions are already answerable

Today we can already answer:

- Did `create`, `detail`, `dashboard`, or `history` slow down?
- Did OpenWeather fall back?
- Did PVGIS fail?
- Did snapshots degrade?
- Did business outcomes shift by origin, recommendation, or attention reason?

Those are the actual demo and support questions that the bounded context needs right now.

### 3. Tracing would add real operational cost

Introducing distributed tracing here would require more than one dependency:

- `io.micrometer:micrometer-tracing-bridge-otel`
- an exporter such as OTLP or Zipkin
- sampling decisions
- collector/backend setup
- log correlation conventions using `traceId` and `spanId`
- review of propagation to outbound HTTP clients

Spring Boot 3.5 documents tracing as an explicit stack on top of Actuator, not something that appears automatically with Prometheus alone. That means this is not a free add-on.

## Important caveat in the current design

The application already uses two different correlation concepts:

- `correlationId` from `CorrelationIdFilter`
- `traceId` generated manually in `LoggingMDCFilter`

This is not only a future tracing concern, it is already a baseline inconsistency today:

- `CorrelationIdFilter` returns `X-Correlation-Id` to clients
- `BaseExceptionHandler` writes the manual `traceId` into `ErrorResponse.correlationId`
- `logback-spring.xml` emits `traceId`, not `correlationId`, in the main MDC pattern

That means clients can receive one public correlation value in headers and a different one in error bodies, while provider logs are easier to find through `traceId`. Before any real distributed tracing is introduced, this baseline must be consolidated. A future tracing slice should first decide whether:

- `correlationId` remains the public request id, and
- framework-managed `traceId` / `spanId` become the tracing identifiers

Do not stack OpenTelemetry tracing on top of the current hand-generated `traceId` semantics without consolidating that design first.

## Where tracing would start adding value later

Tracing becomes more defensible if one or more of these happen:

- more external providers are added to `simulation_service`
- provider calls become parallel or asynchronous
- simulation flows span multiple internal modules with separate infra boundaries
- support incidents require reconstructing request lifecycles across several hops
- a collector such as OTLP or Zipkin already exists as shared platform capability

## Recommended future slice, only if the trigger appears

If tracing becomes necessary, the next slice should stay small:

1. add Micrometer Tracing with OpenTelemetry bridge
2. export only in `local` or `stage` first
3. instrument the `simulation_service` request path plus outbound provider calls
4. fix the current `correlationId` versus `traceId` inconsistency first
5. standardize log correlation on framework-managed `traceId` and `spanId`
6. verify one concrete troubleshooting scenario end to end

That slice should prove diagnostic value before tracing expands to the rest of the platform.

## Recommendation summary

- Keep logs + metrics + health + correlation id as the current observability model.
- Do not add distributed tracing in `#207`.
- Reopen the topic only when `simulation_service` becomes harder to diagnose than the current signals can explain.

## Review checklist

- [ ] The decision is explicit: no tracing for now
- [ ] The reason is cost/benefit, not personal preference
- [ ] The current observability baseline is documented
- [ ] The future trigger for revisiting tracing is concrete
- [ ] The doc calls out the `correlationId` vs hand-generated `traceId` inconsistency as a current baseline issue
- [ ] The doc does not overstate configuration-readiness health indicators as runtime dependency reachability

## Relevant files

- `pom.xml`
- `src/main/java/com/renewsim/backend/shared/exception/handler/BaseExceptionHandler.java`
- `src/main/java/com/renewsim/backend/shared/observability/CorrelationIdFilter.java`
- `src/main/java/com/renewsim/backend/shared/observability/LoggingMDCFilter.java`
- `src/main/java/com/renewsim/backend/simulation_service/infrastructure/health/OpenWeatherSimulationHealthIndicator.java`
- `src/main/java/com/renewsim/backend/simulation_service/infrastructure/health/PvgisSimulationHealthIndicator.java`
- `src/main/resources/logback-spring.xml`
- `docs/architecture/simulation-observability-queries.md`
