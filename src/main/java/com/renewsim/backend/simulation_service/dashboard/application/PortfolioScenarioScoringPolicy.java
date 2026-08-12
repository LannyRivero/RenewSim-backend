package com.renewsim.backend.simulation_service.dashboard.application;

import com.renewsim.backend.simulation_service.dashboard.application.projection.DashboardSnapshotData;
import com.renewsim.backend.simulation_service.domain.model.SimulationRecommendation;
import com.renewsim.backend.simulation_service.domain.policy.SimulationFinancialRiskPolicy;
import com.renewsim.backend.simulation_service.domain.policy.SimulationPortfolioPriorityPolicy;
import com.renewsim.backend.simulation_service.domain.policy.SimulationRecommendationReviewPolicy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Encapsulates portfolio prioritization and risk scoring rules.
 */
@Component
public final class PortfolioScenarioScoringPolicy {

    private final SimulationRecommendationReviewPolicy reviewPolicy = new SimulationRecommendationReviewPolicy();
    private final SimulationFinancialRiskPolicy financialRiskPolicy = new SimulationFinancialRiskPolicy();
    private final SimulationPortfolioPriorityPolicy priorityPolicy = new SimulationPortfolioPriorityPolicy();

    int computeScore(DashboardSnapshotData details, Double roiPercent, Double paybackYears) {
        if (details == null || details.summary() == null) {
            return 20;
        }

        int score = switch (SimulationRecommendation.fromWireValue(details.summary().recommendation())) {
            case RECOMMENDED -> 45;
            case VIABLE_WITH_RESERVATIONS -> 30;
            case NOT_RECOMMENDED -> 10;
        };

        if (roiPercent != null) {
            score += Math.max(0, Math.min(30, (int) Math.round(roiPercent * 1.5)));
        }
        if (paybackYears != null) {
            score += Math.max(0, Math.min(20, (int) Math.round(20.0 - paybackYears)));
        }

        long warnings = safeWarnings(details).stream()
                .filter(warning -> "warning".equalsIgnoreCase(warning.severity()))
                .count();
        score -= (int) warnings * 5;

        return Math.max(0, Math.min(100, score));
    }

    String resolveMainRisk(DashboardSnapshotData details, Double paybackYears, Double roiPercent) {
        return financialRiskPolicy.resolveMainRisk(firstWarningMessage(details), paybackYears, roiPercent);
    }

    boolean needsReview(DashboardSnapshotData details, String recommendation) {
        if (details == null) {
            return true;
        }
        return reviewPolicy.needsReview(
                SimulationRecommendation.fromWireValue(recommendation),
                safeWarnings(details).stream().anyMatch(warning -> "warning".equalsIgnoreCase(warning.severity())),
                true);
    }

    String priorityFor(int score, boolean hasDetails) {
        return priorityPolicy.priorityFor(score, hasDetails);
    }

    boolean hasNegativeRoi(Double roiPercent) {
        return financialRiskPolicy.hasNegativeRoi(roiPercent);
    }

    boolean hasLongPayback(Double paybackYears) {
        return financialRiskPolicy.hasLongPayback(paybackYears);
    }

    private List<DashboardSnapshotData.Warning> safeWarnings(DashboardSnapshotData details) {
        return details.warnings() == null ? List.of() : details.warnings();
    }

    private String firstWarningMessage(DashboardSnapshotData details) {
        if (details == null) {
            return null;
        }
        return safeWarnings(details).stream()
                .filter(warning -> "warning".equalsIgnoreCase(warning.severity()))
                .map(DashboardSnapshotData.Warning::message)
                .findFirst()
                .orElse(null);
    }
}
