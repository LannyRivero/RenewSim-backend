package com.renewsim.backend.simulation_service.shared.application;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationBusinessTelemetryTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final SimulationBusinessTelemetry telemetry = new SimulationBusinessTelemetry(registry);

    @Test
    @DisplayName("recordAttention tracks negative roi and long payback reasons")
    void recordAttentionTracksNegativeRoiAndLongPaybackReasons() {
        telemetry.recordAttention("solar", resultWith(-5.0, 12.0));

        assertThat(registry.counter("simulation_service_business_attention_total", "technology", "solar", "reason", "negative_roi").count())
                .isEqualTo(1.0d);
        assertThat(registry.counter("simulation_service_business_attention_total", "technology", "solar", "reason", "long_payback").count())
                .isEqualTo(1.0d);
        assertThat(registry.counter("simulation_service_business_attention_total", "technology", "solar", "reason", "incomplete_financials").count())
                .isEqualTo(0.0d);
    }

    @Test
    @DisplayName("recordAttention does not treat no-payback results as incomplete financials")
    void recordAttentionDoesNotTreatNoPaybackResultsAsIncompleteFinancials() {
        telemetry.recordAttention("solar", resultWith(8.0, null));

        assertThat(registry.counter("simulation_service_business_attention_total", "technology", "solar", "reason", "incomplete_financials").count())
                .isEqualTo(0.0d);
    }

    @Test
    @DisplayName("recordAttention tracks incomplete financials only when roi and payback are both missing")
    void recordAttentionTracksIncompleteFinancialsOnlyWhenRoiAndPaybackAreBothMissing() {
        telemetry.recordAttention("solar", resultWith(null, null));

        assertThat(registry.counter("simulation_service_business_attention_total", "technology", "solar", "reason", "incomplete_financials").count())
                .isEqualTo(1.0d);
    }

    private SimulationDetailsResult resultWith(Double roiPercent, Double paybackYears) {
        double capex = roiPercent == null ? 0.0 : 100.0;
        double annualBenefit = roiPercent == null ? 0.0 : roiPercent;

        return new SimulationDetailsResult(
                "55",
                "completed",
                "2026-01-01T00:00:00Z",
                "2026-01-01T00:00:00Z",
                "solar-spain-v1",
                "solar",
                null,
                new SimulationDetailsResult.Summary("recommended", "headline", "summary", List.of()),
                new SimulationDetailsResult.Input(
                        "Demo",
                        "solar",
                        null,
                        null,
                        null,
                        new SimulationDetailsResult.Economics("EUR", capex, 0.0, 0.0, 0.0, 0.0, 25)),
                null,
                new SimulationDetailsResult.Financial("EUR", 0.0, 0.0, annualBenefit, paybackYears, null, 0.0, null, 0.0, List.of()),
                null,
                List.of());
    }
}
