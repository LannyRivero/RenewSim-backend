package com.renewsim.backend.simulation_service.dashboard.application;

import com.renewsim.backend.simulation_service.dashboard.application.port.out.DashboardSnapshotReaderPort;
import com.renewsim.backend.simulation_service.dashboard.application.projection.DashboardSnapshotData;
import com.renewsim.backend.simulation_service.dashboard.application.projection.ScenarioSnapshot;
import com.renewsim.backend.simulation_service.dashboard.application.ScenarioSnapshotMetricsResolver.ScenarioSnapshotMetrics;
import com.renewsim.backend.simulation_service.dashboard.application.ScenarioSnapshotNarrativeResolver.ScenarioSnapshotNarrative;
import com.renewsim.backend.simulation_service.shared.application.SimulationReadModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Translates stored simulations into normalized dashboard snapshots.
 */
@Component
@RequiredArgsConstructor
public final class ScenarioSnapshotAssembler {

        private final DashboardSnapshotReaderPort snapshotReader;
        private final PortfolioScenarioScoringPolicy scoringPolicy;
        private final ScenarioSnapshotMetricsResolver metricsResolver;
        private final ScenarioSnapshotNarrativeResolver narrativeResolver;

        public ScenarioSnapshot toSnapshot(SimulationReadModel simulation) {
                DashboardSnapshotData details = readDetails(simulation.resultSnapshot());
                DashboardSnapshotData.Financial financial = details != null ? details.financial() : null;
                DashboardSnapshotData.Technical technical = details != null ? details.technical() : null;
                DashboardSnapshotData.Summary summary = details != null ? details.summary() : null;
                boolean hasCompleteDetails = financial != null && technical != null && summary != null;
                ScenarioSnapshotMetrics metrics = metricsResolver.resolve(simulation, details);
                ScenarioSnapshotNarrative narrative = narrativeResolver.resolve(details);

                int score = scoringPolicy.computeScore(details, metrics.roiPercent(), metrics.paybackYears());

                return new ScenarioSnapshot(
                                String.valueOf(simulation.id()),
                                simulation.name(),
                                normalizeLabel(simulation.technology() != null ? simulation.technology() : "unknown"),
                                normalizeLabel(simulation.status() != null ? simulation.status() : "draft"),
                                simulation.locationLabel(),
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
                                simulation.createdAt(),
                                !hasCompleteDetails,
                                narrativeResolver.needsReview(narrative, hasCompleteDetails),
                                scoringPolicy.hasNegativeRoi(metrics.roiPercent()),
                                scoringPolicy.hasLongPayback(metrics.paybackYears()));
        }

        private DashboardSnapshotData readDetails(String raw) {
                try {
                        return snapshotReader.read(raw);
                } catch (IllegalStateException ex) {
                        return null;
                }
        }

        private String normalizeLabel(String value) {
                return value == null ? "UNKNOWN" : value.trim().toUpperCase(Locale.ROOT);
        }

        private double round(double value) {
                return Math.round(value * 100.0) / 100.0;
        }

}
