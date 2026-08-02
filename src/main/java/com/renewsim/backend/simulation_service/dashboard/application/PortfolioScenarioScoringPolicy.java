package com.renewsim.backend.simulation_service.dashboard.application;

import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;

import java.util.List;

/**
 * Encapsulates portfolio prioritization and risk scoring rules.
 */
final class PortfolioScenarioScoringPolicy {

    private static final double LONG_PAYBACK_YEARS = 10.0;
    private static final double WEAK_ROI_PERCENT = 5.0;

    int computeScore(SimulationDetailsResult details, Double roiPercent, Double paybackYears) {
        if (details == null || details.summary() == null) {
            return 20;
        }

        int score = switch (details.summary().recommendation()) {
            case "recommended" -> 45;
            case "viable_with_reservations" -> 30;
            default -> 10;
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

    String resolveMainRisk(SimulationDetailsResult details, Double paybackYears, Double roiPercent) {
        if (details == null) {
            return "Informacion financiera incompleta";
        }
        String warningRisk = safeWarnings(details).stream()
                .filter(warning -> "warning".equalsIgnoreCase(warning.severity()))
                .map(SimulationDetailsResult.SimulationWarning::message)
                .findFirst()
                .orElse(null);
        if (warningRisk != null) {
            return warningRisk;
        }
        if (paybackYears != null && paybackYears > LONG_PAYBACK_YEARS) {
            return "Payback por encima de la banda esperada";
        }
        if (roiPercent != null && roiPercent < WEAK_ROI_PERCENT) {
            return "Retorno anual debil frente al CAPEX";
        }
        return "Sensibilidad moderada a supuestos economicos";
    }

    boolean needsReview(SimulationDetailsResult details, String recommendation) {
        if (details == null) {
            return true;
        }
        if (!"recommended".equalsIgnoreCase(recommendation)) {
            return true;
        }
        return safeWarnings(details).stream().anyMatch(warning -> "warning".equalsIgnoreCase(warning.severity()));
    }

    String priorityFor(int score, boolean hasDetails) {
        if (!hasDetails) {
            return "REVIEW";
        }
        if (score >= 75) {
            return "HIGH";
        }
        if (score >= 50) {
            return "MEDIUM";
        }
        return "LOW";
    }

    boolean hasNegativeRoi(Double roiPercent) {
        return roiPercent != null && roiPercent < 0.0;
    }

    boolean hasLongPayback(Double paybackYears) {
        return paybackYears != null && paybackYears > LONG_PAYBACK_YEARS;
    }

    private List<SimulationDetailsResult.SimulationWarning> safeWarnings(SimulationDetailsResult details) {
        return details.warnings() == null ? List.of() : details.warnings();
    }
}
