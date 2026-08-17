package com.renewsim.backend.simulation_service.shared.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SimulationBusinessTelemetry {

    private static final double LONG_PAYBACK_YEARS = 10.0;

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();

    public SimulationBusinessTelemetry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordCreated(String technology, boolean fromScenario) {
        counter("simulation_service_business_created_total",
                "technology", normalize(technology),
                "origin", fromScenario ? "scenario" : "direct")
                .increment();
    }

    public void recordRecommendation(String technology, String recommendation) {
        counter("simulation_service_business_recommendation_total",
                "technology", normalize(technology),
                "recommendation", normalize(recommendation))
                .increment();
    }

    public void recordAttention(String technology, SimulationDetailsResult result) {
        Double roiPercent = roiPercent(result);
        Double paybackYears = paybackYears(result);

        if (roiPercent != null && roiPercent < 0.0) {
            counter("simulation_service_business_attention_total",
                    "technology", normalize(technology),
                    "reason", "negative_roi")
                    .increment();
        }

        if (paybackYears != null && paybackYears > LONG_PAYBACK_YEARS) {
            counter("simulation_service_business_attention_total",
                    "technology", normalize(technology),
                    "reason", "long_payback")
                    .increment();
        }

        if (roiPercent == null && paybackYears == null) {
            counter("simulation_service_business_attention_total",
                    "technology", normalize(technology),
                    "reason", "incomplete_financials")
                    .increment();
        }
    }

    private Double roiPercent(SimulationDetailsResult result) {
        if (result == null || result.input() == null || result.input().economics() == null || result.financial() == null) {
            return null;
        }

        double capex = result.input().economics().capexTotal();
        double annualBenefit = result.financial().netAnnualBenefit();
        if (capex <= 0.0) {
            return null;
        }

        return round((annualBenefit / capex) * 100.0);
    }

    private Double paybackYears(SimulationDetailsResult result) {
        if (result == null || result.financial() == null) {
            return null;
        }
        return result.financial().paybackYears();
    }

    private Counter counter(String metricName, String... tags) {
        String key = metricName + ':' + String.join(":", tags);
        return counters.computeIfAbsent(key,
                ignored -> Counter.builder(metricName)
                        .tags(tags)
                        .register(meterRegistry));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
