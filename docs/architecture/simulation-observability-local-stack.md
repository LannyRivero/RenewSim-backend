# Simulation Observability Local Stack

Use this stack when you want a human-friendly local view of the `simulation_service` metrics instead of reading the raw `/actuator/prometheus` text output.

## Quick path

1. Start RenewSim locally on port `8080` with the `local` profile.
2. Start Prometheus + Grafana with:

   ```powershell
   docker compose -f docker-compose.observability.yml up -d
   ```

3. Open Prometheus at `http://localhost:9090`.
4. Open Grafana at `http://localhost:3000` with `admin` / `admin`.

## What this stack does

| Component | Purpose |
|----------|---------|
| Prometheus | Scrapes `http://host.docker.internal:8080/actuator/prometheus` every 15 seconds |
| Grafana | Gives a UI over Prometheus so the metrics are easier to inspect live |

## Why this exists

`/actuator/prometheus` is the correct raw export, but it is not designed for comfortable human reading. This local stack gives a practical bridge between:

- raw metrics exposed by Actuator/Micrometer
- PromQL queries from `simulation-observability-queries.md`
- a more interview-friendly demo surface in Grafana

## Verification checklist

- [ ] RenewSim is running on `localhost:8080`
- [ ] `http://localhost:8080/actuator/prometheus` responds successfully
- [ ] `http://localhost:9090/targets` shows `renewsim-backend` as `UP`
- [ ] Grafana opens at `http://localhost:3000`
- [ ] The Prometheus datasource is preconfigured in Grafana

## First Prometheus checks

Use these in the Prometheus expression bar:

```promql
simulation_service_use_case_total
```

```promql
simulation_service_snapshot_degraded_total
```

```promql
simulation_service_provider_calls_total
```

## Notes

- This setup assumes RenewSim runs on the host machine and Prometheus/Grafana run in Docker.
- `host.docker.internal` is used so the containers can scrape the host app.
- `extra_hosts: host-gateway` is included to improve compatibility where Docker supports it.
- This stack is intentionally minimal. It does not provision dashboards yet.

## Relevant files

- `docker-compose.observability.yml`
- `ops/observability/prometheus/prometheus.yml`
- `ops/observability/grafana/provisioning/datasources/prometheus.yml`
- `docs/architecture/simulation-observability-queries.md`
