package com.renewsim.backend.simulation_service.dashboard.application;

import com.renewsim.backend.simulation_service.dashboard.application.projection.ScenarioSnapshot;
import com.renewsim.backend.simulation_service.dashboard.application.ScenarioSnapshotMetricsResolver.ScenarioSnapshotMetrics;
import com.renewsim.backend.simulation_service.dashboard.application.ScenarioSnapshotNarrativeResolver.ScenarioSnapshotNarrative;
import com.renewsim.backend.simulation_service.shared.application.port.out.SimulationResultSnapshotReaderPort;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Translates stored simulations into normalized dashboard snapshots.
 */
@Component
@RequiredArgsConstructor
public final class ScenarioSnapshotAssembler {

        private final SimulationResultSnapshotReaderPort snapshotReader;
        private final PortfolioScenarioScoringPolicy scoringPolicy;
        private final ScenarioSnapshotMetricsResolver metricsResolver;
        private final ScenarioSnapshotNarrativeResolver narrativeResolver;

        public ScenarioSnapshot toSnapshot(Simulation simulation) {
                SimulationDetailsResult details = readDetails(simulation.getResultSnapshot());
                SimulationDetailsResult.Financial financial = details != null ? details.financial() : null;
                SimulationDetailsResult.Technical technical = details != null ? details.technical() : null;
                SimulationDetailsResult.Summary summary = details != null ? details.summary() : null;
                boolean hasCompleteDetails = financial != null && technical != null && summary != null;
                ScenarioSnapshotMetrics metrics = metricsResolver.resolve(simulation, details);
                ScenarioSnapshotNarrative narrative = narrativeResolver.resolve(details);

                int score = scoringPolicy.computeScore(details, metrics.roiPercent(), metrics.paybackYears());

                return new ScenarioSnapshot(
                                String.valueOf(simulation.getId().value()),
                                simulation.getName(),
                                normalizeLabel(simulation.getTechnology() != null ? simulation.getTechnology().value()
                                                : "unknown"),
                                normalizeLabel(simulation.getStatus() != null ? simulation.getStatus().name()
                                                : "draft"),
                                simulation.getLocation().label(),
                                metrics.roiPercent(),
                                metrics.paybackYears(),
                                metrics.capex(),
                                metrics.annualSavings(),
                                round(metrics.annualGeneration()),
                                round(metrics.co2SavedKg()),
                                narrative.headline(),
                                narrative.drivers(),
                                scoringPolicy.resolveMainRisk(details, metrics.paybackYears(), metrics.roiPercent()),
                                narrative.nextStep(),
                                scoringPolicy.priorityFor(score, hasCompleteDetails),
                                score,
                                simulation.getCreatedAt(),
                                !hasCompleteDetails,
                                narrativeResolver.needsReview(narrative, hasCompleteDetails),
                                scoringPolicy.hasNegativeRoi(metrics.roiPercent()),
                                scoringPolicy.hasLongPayback(metrics.paybackYears()));
        }

        private SimulationDetailsResult readDetails(String raw) {
                return snapshotReader.read(raw);
        }

        private String normalizeLabel(String value) {
                return value == null ? "UNKNOWN" : value.trim().toUpperCase(Locale.ROOT);
        }

        private double round(double value) {
                return Math.round(value * 100.0) / 100.0;
        }

}
