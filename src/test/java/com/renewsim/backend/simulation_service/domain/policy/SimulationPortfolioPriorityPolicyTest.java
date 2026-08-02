package com.renewsim.backend.simulation_service.domain.policy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationPortfolioPriorityPolicyTest {

    private final SimulationPortfolioPriorityPolicy policy = new SimulationPortfolioPriorityPolicy();

    @Test
    @DisplayName("priorityFor maps missing details to review and score bands to priority")
    void priorityForMapsScoreBands() {
        assertThat(policy.priorityFor(80, true)).isEqualTo("HIGH");
        assertThat(policy.priorityFor(60, true)).isEqualTo("MEDIUM");
        assertThat(policy.priorityFor(20, true)).isEqualTo("LOW");
        assertThat(policy.priorityFor(80, false)).isEqualTo("REVIEW");
    }
}
