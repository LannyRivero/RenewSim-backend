package com.renewsim.backend.simulation_service.dashboard.application.projection;

import java.time.LocalDateTime;
import java.util.List;

public record ScenarioSnapshot(
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

    public boolean hasFinancials() {
        return roiPercent != null || paybackYears != null;
    }

    public boolean requiresAttention() {
        return hasIncompleteData || hasNegativeRoi || hasLongPayback || needsReview;
    }
}
