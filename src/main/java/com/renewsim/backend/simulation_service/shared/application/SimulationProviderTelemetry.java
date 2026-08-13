package com.renewsim.backend.simulation_service.shared.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SimulationProviderTelemetry {

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>();

    public SimulationProviderTelemetry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample start() {
        return Timer.start(meterRegistry);
    }

    public void recordSuccess(String provider, Timer.Sample sample) {
        record(provider, "success", sample);
    }

    public void recordFallback(String provider, Timer.Sample sample) {
        record(provider, "fallback", sample);
    }

    public void recordError(String provider, Timer.Sample sample) {
        record(provider, "error", sample);
    }

    private void record(String provider, String outcome, Timer.Sample sample) {
        counter(provider, outcome).increment();
        sample.stop(timer(provider, outcome));
    }

    private Counter counter(String provider, String outcome) {
        return counters.computeIfAbsent(provider + ':' + outcome,
                ignored -> Counter.builder("simulation_service_provider_calls_total")
                        .tag("provider", provider)
                        .tag("outcome", outcome)
                        .register(meterRegistry));
    }

    private Timer timer(String provider, String outcome) {
        return timers.computeIfAbsent(provider + ':' + outcome,
                ignored -> Timer.builder("simulation_service_provider_call_duration")
                        .tag("provider", provider)
                        .tag("outcome", outcome)
                        .register(meterRegistry));
    }
}
