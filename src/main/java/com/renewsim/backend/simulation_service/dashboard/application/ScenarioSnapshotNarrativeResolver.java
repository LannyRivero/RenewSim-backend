package com.renewsim.backend.simulation_service.dashboard.application;

import com.renewsim.backend.simulation_service.dashboard.application.projection.DashboardSnapshotData;
import com.renewsim.backend.simulation_service.domain.model.SimulationRecommendation;
import com.renewsim.backend.simulation_service.domain.policy.SimulationRecommendationReviewPolicy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class ScenarioSnapshotNarrativeResolver {

        private final SimulationRecommendationReviewPolicy reviewPolicy = new SimulationRecommendationReviewPolicy();

        public ScenarioSnapshotNarrative resolve(DashboardSnapshotData details) {
                DashboardSnapshotData.Summary summary = details != null ? details.summary() : null;
                String recommendation = summary != null && summary.recommendation() != null
                                ? summary.recommendation()
                                : "review_required";
                SimulationRecommendation normalizedRecommendation = SimulationRecommendation
                                .fromWireValue(recommendation);

                List<String> drivers = summary != null && summary.reasons() != null
                                ? summary.reasons().stream()
                                                .map(DashboardSnapshotData.Reason::message)
                                                .filter(message -> message != null && !message.isBlank())
                                                .limit(3)
                                                .toList()
                                : List.of("El escenario todavia no tiene salida financiera consolidada.");
                if (drivers.isEmpty()) {
                        drivers = List.of("El escenario todavia no tiene salida financiera consolidada.");
                }

                String headline = summary != null && summary.headline() != null
                                ? summary.headline()
                                : "El escenario necesita completar datos antes de priorizarse.";

                boolean hasWarnings = details != null && details.warnings() != null
                                && details.warnings().stream()
                                                .anyMatch(warning -> "warning".equalsIgnoreCase(warning.severity()));

                return new ScenarioSnapshotNarrative(normalizedRecommendation, drivers, headline, hasWarnings,
                                reviewPolicy.nextStepFor(normalizedRecommendation));
        }

        public boolean needsReview(ScenarioSnapshotNarrative narrative, boolean hasCompleteDetails) {
                return reviewPolicy.needsReview(narrative.recommendation(), narrative.hasWarnings(),
                                hasCompleteDetails);
        }

        public record ScenarioSnapshotNarrative(
                        SimulationRecommendation recommendation,
                        List<String> drivers,
                        String headline,
                        boolean hasWarnings,
                        String nextStep) {
        }
}
