package com.renewsim.backend.simulation_service.dashboard.application;

import com.renewsim.backend.simulation_service.domain.model.SimulationRecommendation;
import com.renewsim.backend.simulation_service.domain.policy.SimulationRecommendationReviewPolicy;
import com.renewsim.backend.simulation_service.dashboard.application.projection.ScenarioSnapshot;
import com.renewsim.backend.simulation_service.shared.application.port.out.SimulationResultSnapshotReaderPort;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Translates stored simulations into normalized dashboard snapshots.
 */
@Component
@RequiredArgsConstructor
public final class ScenarioSnapshotAssembler {

        private final TechnologyLookupPort technologyLookupPort;
        private final SimulationResultSnapshotReaderPort snapshotReader;
        private final PortfolioScenarioScoringPolicy scoringPolicy;
        private final SimulationRecommendationReviewPolicy reviewPolicy = new SimulationRecommendationReviewPolicy();

        public ScenarioSnapshot toSnapshot(Simulation simulation) {
                SimulationDetailsResult details = readDetails(simulation.getResultSnapshot());
                SimulationDetailsResult.Financial financial = details != null ? details.financial() : null;
                SimulationDetailsResult.Technical technical = details != null ? details.technical() : null;
                SimulationDetailsResult.Summary summary = details != null ? details.summary() : null;
                boolean hasCompleteDetails = financial != null && technical != null && summary != null;
                Double capex = simulation.getEconomics() != null ? simulation.getEconomics().capexTotal() : null;
                Double annualSavings = financial != null ? Double.valueOf(financial.annualSavings())
                                : simulation.getAnnualSavings();
                Double annualBenefit = financial != null ? Double.valueOf(financial.netAnnualBenefit())
                                : simulation.getAnnualSavings();
                Double paybackYears = financial != null ? financial.paybackYears() : null;
                Double roiPercent = roiPercent(capex, annualBenefit);
                double annualGeneration = technical != null ? technical.annualGenerationKwh()
                                : defaultNumber(simulation.getAnnualGenerationKwh());
                double co2SavedKg = annualGeneration > 0.0 && simulation.getTechnology() != null
                                ? technologyLookupPort
                                                .findActiveCo2ReductionFactorByEnergyType(
                                                                simulation.getTechnology().value())
                                                .map(factor -> annualGeneration * factor)
                                                .orElse(0.0)
                                : 0.0;
                String recommendation = summary != null && summary.recommendation() != null
                                ? summary.recommendation()
                                : "review_required";
                SimulationRecommendation normalizedRecommendation = SimulationRecommendation
                                .fromWireValue(recommendation);
                List<String> drivers = summary != null && summary.reasons() != null
                                ? summary.reasons().stream()
                                                .map(SimulationDetailsResult.RecommendationReason::message)
                                                .filter(message -> message != null && !message.isBlank())
                                                .limit(3)
                                                .toList()
                                : List.of("El escenario todavia no tiene salida financiera consolidada.");
                if (drivers.isEmpty()) {
                        drivers = List.of("El escenario todavia no tiene salida financiera consolidada.");
                }

                int score = scoringPolicy.computeScore(details, roiPercent, paybackYears);

                return new ScenarioSnapshot(
                                String.valueOf(simulation.getId().value()),
                                simulation.getName(),
                                normalizeLabel(simulation.getTechnology() != null ? simulation.getTechnology().value()
                                                : "unknown"),
                                normalizeLabel(simulation.getStatus() != null ? simulation.getStatus().name()
                                                : "draft"),
                                simulation.getLocation().label(),
                                roiPercent,
                                paybackYears,
                                capex,
                                annualSavings,
                                round(annualGeneration),
                                round(co2SavedKg),
                                summary != null && summary.headline() != null
                                                ? summary.headline()
                                                : "El escenario necesita completar datos antes de priorizarse.",
                                drivers,
                                scoringPolicy.resolveMainRisk(details, paybackYears, roiPercent),
                                reviewPolicy.nextStepFor(normalizedRecommendation),
                                scoringPolicy.priorityFor(score, hasCompleteDetails),
                                score,
                                simulation.getCreatedAt(),
                                !hasCompleteDetails,
                                reviewPolicy.needsReview(
                                                normalizedRecommendation,
                                                details != null && details.warnings() != null
                                                                && details.warnings().stream().anyMatch(
                                                                                warning -> "warning".equalsIgnoreCase(
                                                                                                warning.severity())),
                                                hasCompleteDetails),
                                scoringPolicy.hasNegativeRoi(roiPercent),
                                scoringPolicy.hasLongPayback(paybackYears));
        }

        private SimulationDetailsResult readDetails(String raw) {
                return snapshotReader.read(raw);
        }

        private Double roiPercent(Double capex, Double annualBenefit) {
                if (capex == null || annualBenefit == null || capex <= 0.0) {
                        return null;
                }
                return round((annualBenefit / capex) * 100.0);
        }

        private String normalizeLabel(String value) {
                return value == null ? "UNKNOWN" : value.trim().toUpperCase(Locale.ROOT);
        }

        private double round(double value) {
                return Math.round(value * 100.0) / 100.0;
        }

        private double defaultNumber(Double value) {
                return value == null ? 0.0 : value;
        }
}
