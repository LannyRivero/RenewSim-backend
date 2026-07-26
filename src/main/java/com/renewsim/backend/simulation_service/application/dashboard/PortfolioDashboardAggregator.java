package com.renewsim.backend.simulation_service.application.dashboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates ranked scenario snapshots into the portfolio dashboard view model.
 */
final class PortfolioDashboardAggregator {

    PortfolioDashboardResult buildDashboard(List<ScenarioSnapshot> snapshots, List<ScenarioSnapshot> rankedSnapshots) {
        return new PortfolioDashboardResult(
                buildSummary(snapshots),
                buildRecommendedScenario(rankedSnapshots),
                rankedSnapshots.stream().map(this::toPrioritizedScenario).toList(),
                buildRiskAlerts(snapshots),
                buildDistribution(snapshots));
    }

    private PortfolioDashboardSummary buildSummary(List<ScenarioSnapshot> snapshots) {
        List<Double> roiValues = snapshots.stream()
                .map(ScenarioSnapshot::roiPercent)
                .filter(value -> value != null)
                .sorted()
                .toList();
        List<Double> paybackValues = snapshots.stream()
                .map(ScenarioSnapshot::paybackYears)
                .filter(value -> value != null)
                .sorted()
                .toList();

        return new PortfolioDashboardSummary(
                snapshots.size(),
                snapshots.size(),
                average(roiValues),
                median(paybackValues),
                round(snapshots.stream().mapToDouble(ScenarioSnapshot::annualGenerationKwh).sum()),
                round(snapshots.stream().mapToDouble(ScenarioSnapshot::co2SavedKg).sum()),
                snapshots.stream().filter(ScenarioSnapshot::requiresAttention).count());
    }

    private PortfolioDashboardRecommendedScenario buildRecommendedScenario(List<ScenarioSnapshot> rankedSnapshots) {
        return rankedSnapshots.stream()
                .filter(ScenarioSnapshot::hasFinancials)
                .findFirst()
                .map(snapshot -> new PortfolioDashboardRecommendedScenario(
                        snapshot.id(),
                        snapshot.name(),
                        snapshot.technology(),
                        snapshot.location(),
                        snapshot.roiPercent(),
                        snapshot.paybackYears(),
                        snapshot.capex(),
                        snapshot.estimatedAnnualSavings(),
                        snapshot.priority(),
                        snapshot.headline(),
                        snapshot.drivers(),
                        snapshot.mainRisk(),
                        snapshot.nextStep()))
                .orElse(null);
    }

    private List<PortfolioDashboardRiskAlert> buildRiskAlerts(List<ScenarioSnapshot> snapshots) {
        List<PortfolioDashboardRiskAlert> alerts = new ArrayList<>();

        long negativeRoi = snapshots.stream().filter(ScenarioSnapshot::hasNegativeRoi).count();
        if (negativeRoi > 0) {
            alerts.add(new PortfolioDashboardRiskAlert(
                    "NEGATIVE_ROI",
                    "HIGH",
                    negativeRoi,
                    negativeRoi + " escenarios presentan ROI negativo"));
        }

        long longPayback = snapshots.stream().filter(ScenarioSnapshot::hasLongPayback).count();
        if (longPayback > 0) {
            alerts.add(new PortfolioDashboardRiskAlert(
                    "LONG_PAYBACK",
                    "MEDIUM",
                    longPayback,
                    longPayback + " escenarios tienen un payback demasiado largo"));
        }

        long incompleteData = snapshots.stream().filter(ScenarioSnapshot::hasIncompleteData).count();
        if (incompleteData > 0) {
            alerts.add(new PortfolioDashboardRiskAlert(
                    "INCOMPLETE_DATA",
                    "MEDIUM",
                    incompleteData,
                    incompleteData + " simulaciones no tienen informacion suficiente para priorizar"));
        }

        long reviewRequired = snapshots.stream().filter(ScenarioSnapshot::needsReview).count();
        if (reviewRequired > 0) {
            alerts.add(new PortfolioDashboardRiskAlert(
                    "REQUIRES_REVIEW",
                    "LOW",
                    reviewRequired,
                    reviewRequired + " escenarios requieren revision ejecutiva antes de avanzar"));
        }

        return alerts;
    }

    private PortfolioDashboardDistribution buildDistribution(List<ScenarioSnapshot> snapshots) {
        Map<String, List<ScenarioSnapshot>> byTechnology = new LinkedHashMap<>();
        Map<String, Long> byStatus = new LinkedHashMap<>();

        for (ScenarioSnapshot snapshot : snapshots) {
            byTechnology.computeIfAbsent(snapshot.technology(), key -> new ArrayList<>()).add(snapshot);
            byStatus.merge(snapshot.status(), 1L, Long::sum);
        }

        List<PortfolioDashboardDistributionByTechnology> byTechnologyItems = byTechnology.entrySet().stream()
                .map(entry -> new PortfolioDashboardDistributionByTechnology(
                        entry.getKey(),
                        entry.getValue().size(),
                        round(entry.getValue().stream().mapToDouble(ScenarioSnapshot::annualGenerationKwh).sum())))
                .sorted(Comparator.comparingLong(PortfolioDashboardDistributionByTechnology::count).reversed())
                .toList();

        List<PortfolioDashboardDistributionByStatus> byStatusItems = byStatus.entrySet().stream()
                .map(entry -> new PortfolioDashboardDistributionByStatus(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(PortfolioDashboardDistributionByStatus::count).reversed())
                .toList();

        return new PortfolioDashboardDistribution(byTechnologyItems, byStatusItems);
    }

    private PortfolioDashboardPrioritizedScenario toPrioritizedScenario(ScenarioSnapshot snapshot) {
        return new PortfolioDashboardPrioritizedScenario(
                snapshot.id(),
                snapshot.name(),
                snapshot.technology(),
                snapshot.status(),
                snapshot.location(),
                snapshot.roiPercent(),
                snapshot.paybackYears(),
                snapshot.capex(),
                snapshot.estimatedAnnualSavings(),
                snapshot.priority(),
                snapshot.score());
    }

    private Double average(List<Double> values) {
        if (values.isEmpty()) {
            return null;
        }
        return round(values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
    }

    private Double median(List<Double> values) {
        if (values.isEmpty()) {
            return null;
        }
        int middle = values.size() / 2;
        if (values.size() % 2 == 0) {
            return round((values.get(middle - 1) + values.get(middle)) / 2.0);
        }
        return round(values.get(middle));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
