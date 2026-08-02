package com.renewsim.backend.simulation_service.history.web.dto;

public record SimulationHistoryRowDTO(
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
