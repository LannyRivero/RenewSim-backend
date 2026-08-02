package com.renewsim.backend.simulation_service.domain.policy;

import com.renewsim.backend.simulation_service.domain.model.SimulationRecommendation;

public final class SolarSimulationRecommendationPolicy {

        public RecommendationDecision decide(
                        double npv,
                        Double irrPct,
                        double discountRatePct,
                        Double paybackYears,
                        int projectLifetimeYears,
                        double specificYield) {

                if (npv > 0
                                && irrPct != null
                                && irrPct >= discountRatePct
                                && paybackYears != null
                                && paybackYears <= projectLifetimeYears / 2.0
                                && specificYield >= 1250) {
                        return new RecommendationDecision(
                                        SimulationRecommendation.RECOMMENDED,
                                        "The scenario is technically solid and clears the baseline financial gate.",
                                        "Resource quality, annual yield, and discounted returns support moving the case into detailed engineering and commercial validation.");
                }

                if (npv > 0 || (paybackYears != null && paybackYears <= projectLifetimeYears)) {
                        return new RecommendationDecision(
                                        SimulationRecommendation.VIABLE_WITH_RESERVATIONS,
                                        "The scenario is viable, but the decision depends on validating core assumptions.",
                                        "The project shows credible technical output, while the financial profile still requires executive review of pricing, losses, and recovery targets.");
                }

                return new RecommendationDecision(
                                SimulationRecommendation.NOT_RECOMMENDED,
                                "The scenario should not move forward without material assumption changes.",
                                "Under the submitted inputs, the technical and financial outputs do not justify progressing the case in its current form.");
        }

        public record RecommendationDecision(
                        SimulationRecommendation recommendation,
                        String headline,
                        String summary) {
        }
}
