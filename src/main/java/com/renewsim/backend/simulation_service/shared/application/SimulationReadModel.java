package com.renewsim.backend.simulation_service.shared.application;

import java.time.LocalDateTime;

public record SimulationReadModel(
                Long id,
                String name,
                String technology,
                String status,
                String locationLabel,
                Double annualGenerationKwh,
                Double annualSavings,
                Double npv,
                Double irrPct,
                String recommendation,
                Double capexTotal,
                String resultSnapshot,
                LocalDateTime createdAt) {
}
