package com.renewsim.backend.simulation_service.application.dashboard;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PortfolioDashboardAggregatorTest {

        private final PortfolioDashboardAggregator aggregator = new PortfolioDashboardAggregator();

        @Test
        @DisplayName("buildDashboard aggregates summary, recommendation, alerts and distribution")
        void buildDashboardAggregatesSummaryRecommendationAlertsAndDistribution() {
                ScenarioSnapshot best = snapshot(
                                "55", "SOLAR", "COMPLETED", 22.5, 6.2, 315000.0, 82000.0,
                                457200.0, 205740.0, "HIGH", 82,
                                false, false, false, false);
                ScenarioSnapshot risky = snapshot(
                                "56", "SOLAR", "COMPLETED", -1.59, 12.4, 315000.0, 12000.0,
                                350000.0, 157500.0, "LOW", 18,
                                false, true, true, true);
                ScenarioSnapshot draft = snapshot(
                                "57", "WIND", "DRAFT", null, null, 110000.0, null,
                                0.0, 0.0, "REVIEW", 20,
                                true, true, false, false);

                PortfolioDashboardResult result = aggregator.buildDashboard(
                                List.of(best, risky, draft),
                                List.of(best, draft, risky));

                assertThat(result.summary().totalSimulations()).isEqualTo(3);
                assertThat(result.summary().averageRoiPercent()).isEqualTo(10.46);
                assertThat(result.summary().medianPaybackYears()).isEqualTo(9.3);
                assertThat(result.summary().totalEnergyGeneratedKwh()).isEqualTo(807200.0);
                assertThat(result.summary().totalCo2SavedKg()).isEqualTo(363240.0);
                assertThat(result.summary().atRiskCount()).isEqualTo(2);

                assertThat(result.recommendedScenario()).isNotNull();
                assertThat(result.recommendedScenario().id()).isEqualTo("55");
                assertThat(result.prioritizedScenarios()).extracting(PortfolioDashboardPrioritizedScenario::id)
                                .containsExactly("55", "57", "56");

                assertThat(result.riskAlerts()).extracting(PortfolioDashboardRiskAlert::type)
                                .containsExactly("NEGATIVE_ROI", "LONG_PAYBACK", "INCOMPLETE_DATA", "REQUIRES_REVIEW");

                assertThat(result.distribution().byTechnology())
                                .extracting(PortfolioDashboardDistributionByTechnology::label)
                                .containsExactly("SOLAR", "WIND");
                assertThat(result.distribution().byTechnology().getFirst().energyKwh()).isEqualTo(807200.0);
                assertThat(result.distribution().byStatus()).extracting(PortfolioDashboardDistributionByStatus::label)
                                .containsExactly("COMPLETED", "DRAFT");
        }

        @Test
        @DisplayName("buildDashboard leaves recommended scenario null when ranked snapshots have no financials")
        void buildDashboardLeavesRecommendedScenarioNullWhenNoFinancialsExist() {
                ScenarioSnapshot review = snapshot(
                                "57", "SOLAR", "DRAFT", null, null, 110000.0, null,
                                0.0, 0.0, "REVIEW", 20,
                                true, true, false, false);

                PortfolioDashboardResult result = aggregator.buildDashboard(List.of(review), List.of(review));

                assertThat(result.recommendedScenario()).isNull();
                assertThat(result.riskAlerts()).extracting(PortfolioDashboardRiskAlert::type)
                                .containsExactly("INCOMPLETE_DATA", "REQUIRES_REVIEW");
        }

        private ScenarioSnapshot snapshot(
                        String id,
                        String technology,
                        String status,
                        Double roiPercent,
                        Double paybackYears,
                        Double capex,
                        Double annualSavings,
                        double annualGenerationKwh,
                        double co2SavedKg,
                        String priority,
                        int score,
                        boolean hasIncompleteData,
                        boolean needsReview,
                        boolean hasNegativeRoi,
                        boolean hasLongPayback) {
                return new ScenarioSnapshot(
                                id,
                                "Scenario " + id,
                                technology,
                                status,
                                "Sevilla, ES",
                                roiPercent,
                                paybackYears,
                                capex,
                                annualSavings,
                                annualGenerationKwh,
                                co2SavedKg,
                                "headline",
                                List.of("driver"),
                                "risk",
                                "next step",
                                priority,
                                score,
                                LocalDateTime.parse("2026-06-30T14:00:00"),
                                hasIncompleteData,
                                needsReview,
                                hasNegativeRoi,
                                hasLongPayback);
        }
}
