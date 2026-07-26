package com.renewsim.backend.simulation_service.application.dashboard;

import com.renewsim.backend.simulation_service.application.shared.SimulationDetailsResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PortfolioScenarioScoringPolicyTest {

    private final PortfolioScenarioScoringPolicy policy = new PortfolioScenarioScoringPolicy();

    @Test
    @DisplayName("computeScore combines recommendation, roi, payback and warnings")
    void computeScoreCombinesRecommendationRoiPaybackAndWarnings() {
        SimulationDetailsResult details = details(
                "recommended",
                List.of(new SimulationDetailsResult.SimulationWarning("warning", "LOW_AVAILABILITY",
                        "warning message")));

        int score = policy.computeScore(details, 20.0, 5.0);

        assertThat(score).isEqualTo(85);
    }

    @Test
    @DisplayName("computeScore falls back to baseline when summary is unavailable")
    void computeScoreFallsBackToBaselineWhenSummaryIsUnavailable() {
        assertThat(policy.computeScore(null, 20.0, 5.0)).isEqualTo(20);
    }

    @Test
    @DisplayName("resolveMainRisk prefers warning messages over generic thresholds")
    void resolveMainRiskPrefersWarningMessagesOverGenericThresholds() {
        SimulationDetailsResult details = details(
                "viable_with_reservations",
                List.of(new SimulationDetailsResult.SimulationWarning("warning", "LOW_AVAILABILITY",
                        "Availability too low")));

        String mainRisk = policy.resolveMainRisk(details, 12.0, 2.0);

        assertThat(mainRisk).isEqualTo("Availability too low");
    }

    @Test
    @DisplayName("priorityFor maps review and score bands explicitly")
    void priorityForMapsReviewAndScoreBandsExplicitly() {
        assertThat(policy.priorityFor(80, true)).isEqualTo("HIGH");
        assertThat(policy.priorityFor(60, true)).isEqualTo("MEDIUM");
        assertThat(policy.priorityFor(20, true)).isEqualTo("LOW");
        assertThat(policy.priorityFor(80, false)).isEqualTo("REVIEW");
    }

    @Test
    @DisplayName("needsReview only clears recommended scenarios without warnings")
    void needsReviewOnlyClearsRecommendedScenariosWithoutWarnings() {
        SimulationDetailsResult cleanRecommended = details("recommended", List.of());
        SimulationDetailsResult warnedRecommended = details(
                "recommended",
                List.of(new SimulationDetailsResult.SimulationWarning("warning", "LOW_AVAILABILITY",
                        "Availability too low")));

        assertThat(policy.needsReview(cleanRecommended, "recommended")).isFalse();
        assertThat(policy.needsReview(warnedRecommended, "recommended")).isTrue();
        assertThat(policy.needsReview(cleanRecommended, "viable_with_reservations")).isTrue();
        assertThat(policy.needsReview(null, "recommended")).isTrue();
    }

    @Test
    @DisplayName("negative roi and long payback flags follow threshold rules")
    void negativeRoiAndLongPaybackFlagsFollowThresholdRules() {
        assertThat(policy.hasNegativeRoi(-0.1)).isTrue();
        assertThat(policy.hasNegativeRoi(0.0)).isFalse();
        assertThat(policy.hasLongPayback(10.1)).isTrue();
        assertThat(policy.hasLongPayback(10.0)).isFalse();
    }

    private SimulationDetailsResult details(
            String recommendation,
            List<SimulationDetailsResult.SimulationWarning> warnings) {
        return new SimulationDetailsResult(
                "55",
                "completed",
                "2026-06-30T14:00:00Z",
                "2026-06-30T14:00:00Z",
                "solar-spain-v1",
                "solar",
                null,
                new SimulationDetailsResult.Summary(recommendation, "headline", "summary", List.of()),
                null,
                null,
                null,
                null,
                warnings);
    }
}
