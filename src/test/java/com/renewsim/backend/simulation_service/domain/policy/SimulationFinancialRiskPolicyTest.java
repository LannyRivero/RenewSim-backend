package com.renewsim.backend.simulation_service.domain.policy;

import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationFinancialRiskPolicyTest {

    private final SimulationFinancialRiskPolicy policy = new SimulationFinancialRiskPolicy();

    @Test
    @DisplayName("resolveMainRisk prefers explicit warnings")
    void resolveMainRiskPrefersWarnings() {
        SimulationDetailsResult details = details(List.of(
                new SimulationDetailsResult.SimulationWarning("warning", "LOW_AVAILABILITY", "Availability too low")));

        assertThat(policy.resolveMainRisk(details, 12.0, 2.0)).isEqualTo("Availability too low");
    }

    @Test
    @DisplayName("resolveMainRisk falls back to payback and roi thresholds")
    void resolveMainRiskFallsBackToThresholds() {
        assertThat(policy.resolveMainRisk(details(List.of()), 12.0, 6.0))
                .contains("Payback");
        assertThat(policy.resolveMainRisk(details(List.of()), 8.0, 2.0))
                .contains("Retorno anual");
    }

    @Test
    @DisplayName("financial flags follow threshold rules")
    void financialFlagsFollowThresholdRules() {
        assertThat(policy.hasNegativeRoi(-0.1)).isTrue();
        assertThat(policy.hasNegativeRoi(0.0)).isFalse();
        assertThat(policy.hasLongPayback(10.1)).isTrue();
        assertThat(policy.hasLongPayback(10.0)).isFalse();
        assertThat(policy.hasWeakRoi(4.9)).isTrue();
        assertThat(policy.hasWeakRoi(5.0)).isFalse();
    }

    private SimulationDetailsResult details(List<SimulationDetailsResult.SimulationWarning> warnings) {
        return new SimulationDetailsResult(
                "55",
                "completed",
                "2026-06-30T14:00:00Z",
                "2026-06-30T14:00:00Z",
                "solar-spain-v1",
                "solar",
                null,
                new SimulationDetailsResult.Summary("recommended", "headline", "summary", List.of()),
                null,
                null,
                null,
                null,
                warnings);
    }
}
