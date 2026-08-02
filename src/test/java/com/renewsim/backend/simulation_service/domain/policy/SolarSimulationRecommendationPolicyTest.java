package com.renewsim.backend.simulation_service.domain.policy;

import com.renewsim.backend.simulation_service.domain.model.SimulationRecommendation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SolarSimulationRecommendationPolicyTest {

    private final SolarSimulationRecommendationPolicy policy = new SolarSimulationRecommendationPolicy();

    @Test
    @DisplayName("decide returns recommended for strong financial and resource profile")
    void decideReturnsRecommendedForStrongProfile() {
        SolarSimulationRecommendationPolicy.RecommendationDecision decision = policy.decide(
                121500.0,
                14.2,
                8.0,
                6.0,
                20,
                1400.0);

        assertThat(decision.recommendation()).isEqualTo(SimulationRecommendation.RECOMMENDED);
    }

    @Test
    @DisplayName("decide returns viable with reservations when project is recoverable but not strong")
    void decideReturnsViableWithReservationsWhenRecoverable() {
        SolarSimulationRecommendationPolicy.RecommendationDecision decision = policy.decide(
                -5000.0,
                null,
                8.0,
                18.0,
                20,
                1180.0);

        assertThat(decision.recommendation()).isEqualTo(SimulationRecommendation.VIABLE_WITH_RESERVATIONS);
    }

    @Test
    @DisplayName("decide returns not recommended when the case does not recover investment")
    void decideReturnsNotRecommendedWhenWeak() {
        SolarSimulationRecommendationPolicy.RecommendationDecision decision = policy.decide(
                -25000.0,
                null,
                8.0,
                25.0,
                20,
                1000.0);

        assertThat(decision.recommendation()).isEqualTo(SimulationRecommendation.NOT_RECOMMENDED);
    }
}
