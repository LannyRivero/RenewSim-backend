package com.renewsim.backend.simulation_service.domain.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;

class SimulationFinancialRiskPolicyTest {

    private final SimulationFinancialRiskPolicy policy = new SimulationFinancialRiskPolicy();

    @Test
    @DisplayName("resolveMainRisk prefers explicit warnings")
    void resolveMainRiskPrefersWarnings() {
        assertThat(policy.resolveMainRisk("Availability too low", 12.0, 2.0)).isEqualTo("Availability too low");
    }

    @Test
    @DisplayName("resolveMainRisk falls back to payback and roi thresholds")
    void resolveMainRiskFallsBackToThresholds() {
        assertThat(policy.resolveMainRisk(null, 12.0, 6.0))
                .contains("Payback");
        assertThat(policy.resolveMainRisk(null, 8.0, 2.0))
                .contains("Retorno anual");
        assertThat(policy.resolveMainRisk(null, null, null))
                .contains("incompleta");
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
}
