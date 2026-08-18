package com.renewsim.backend.simulation_service.shared.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Technical counters and timers for simulation use cases.
 *
 * <p>Exposes low-cardinality {@code simulation_service_use_case_total} counters and
 * {@code simulation_service_use_case_duration} timers tagged by {@code use_case} and
 * {@code outcome} ({@code success}/{@code error}/{@code degraded}). Meter instances are cached
 * per tag combination to avoid re-registering the same meter on every call. Callers decide when
 * to record; the create/update services only record success after the transaction commits.</p>
 */
@Component
public class SimulationUseCaseTelemetry {

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>();

    public SimulationUseCaseTelemetry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample start() {
        return Timer.start(meterRegistry);
    }

    public void recordSuccess(String useCase, Timer.Sample sample) {
        record(useCase, "success", sample);
    }

    public void recordError(String useCase, Timer.Sample sample) {
        record(useCase, "error", sample);
    }

    public void recordDegraded(String useCase, Timer.Sample sample) {
        record(useCase, "degraded", sample);
    }

    public void recordSnapshotDegraded(String reason) {
        Counter.builder("simulation_service_snapshot_degraded_total")
                .tag("reason", reason)
                .register(meterRegistry)
                .increment();
    }

    private void record(String useCase, String outcome, Timer.Sample sample) {
        counter(useCase, outcome).increment();
        sample.stop(timer(useCase, outcome));
    }

    private Counter counter(String useCase, String outcome) {
        return counters.computeIfAbsent(useCase + ':' + outcome,
                ignored -> Counter.builder("simulation_service_use_case_total")
                        .tag("use_case", useCase)
                        .tag("outcome", outcome)
                        .register(meterRegistry));
    }

    private Timer timer(String useCase, String outcome) {
        return timers.computeIfAbsent(useCase + ':' + outcome,
                ignored -> Timer.builder("simulation_service_use_case_duration")
                        .tag("use_case", useCase)
                        .tag("outcome", outcome)
                        .register(meterRegistry));
    }
}
