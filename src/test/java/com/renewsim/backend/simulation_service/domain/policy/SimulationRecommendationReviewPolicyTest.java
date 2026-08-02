package com.renewsim.backend.simulation_service.domain.policy;

import com.renewsim.backend.simulation_service.domain.model.SimulationRecommendation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationRecommendationReviewPolicyTest {

    private final SimulationRecommendationReviewPolicy policy = new SimulationRecommendationReviewPolicy();

    @Test
    @DisplayName("needsReview is false only for recommended scenarios with details and no warnings")
    void needsReviewIsFalseOnlyForCleanRecommendedScenarios() {
        assertThat(policy.needsReview(SimulationRecommendation.RECOMMENDED, false, true)).isFalse();
        assertThat(policy.needsReview(SimulationRecommendation.RECOMMENDED, true, true)).isTrue();
        assertThat(policy.needsReview(SimulationRecommendation.VIABLE_WITH_RESERVATIONS, false, true)).isTrue();
        assertThat(policy.needsReview(SimulationRecommendation.NOT_RECOMMENDED, false, true)).isTrue();
        assertThat(policy.needsReview(SimulationRecommendation.RECOMMENDED, false, false)).isTrue();
    }

    @Test
    @DisplayName("nextStepFor maps each recommendation to an explicit business next step")
    void nextStepForMapsRecommendations() {
        assertThat(policy.nextStepFor(SimulationRecommendation.RECOMMENDED))
                .contains("evaluacion ejecutiva");
        assertThat(policy.nextStepFor(SimulationRecommendation.VIABLE_WITH_RESERVATIONS))
                .contains("supuestos criticos");
        assertThat(policy.nextStepFor(SimulationRecommendation.NOT_RECOMMENDED))
                .contains("Completar datos");
    }
}
