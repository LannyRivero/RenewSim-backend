package com.renewsim.backend.simulation_service.application.dashboard;

import java.time.LocalDateTime;
import java.util.List;

record ScenarioSnapshot(
        String id,
        String name,
        String technology,
        String status,
        String location,
        Double roiPercent,
        Double paybackYears,
        Double capex,
        Double estimatedAnnualSavings,
        double annualGenerationKwh,
        double co2SavedKg,
        String headline,
        List<String> drivers,
        String mainRisk,
        String nextStep,
        String priority,
        int score,
        LocalDateTime createdAt,
        boolean hasIncompleteData,
        boolean needsReview,
        boolean hasNegativeRoi,
        boolean hasLongPayback) {

    boolean hasFinancials() {
        return roiPercent != null || paybackYears != null;
    }

    boolean requiresAttention() {
        return hasIncompleteData || hasNegativeRoi || hasLongPayback || needsReview;
    }
}
