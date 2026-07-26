package com.renewsim.backend.simulation_service.application.historySimulation;

public record SimulationHistoryRowResult(
        String id,
        String name,
        String technology,
        String status,
        String createdAt,
        String locationLabel,
        double annualGenerationKwh,
        double annualSavings,
        double npv,
        Double irrPct,
        String recommendation,
        String modelVersion,
        String resourceSource) {
}
