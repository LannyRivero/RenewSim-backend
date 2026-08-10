package com.renewsim.backend.simulation_service.domain.model;

import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationCompletionException;

import java.util.List;

public record SimulationCompletion(
        String resultSnapshot,
        Double annualGenerationKwh,
        Double annualSavings,
        Double npv,
        Double irrPct,
        String recommendation,
        List<Long> technologyIds) {

    public SimulationCompletion {
        if (resultSnapshot == null || resultSnapshot.isBlank()) {
            throw new InvalidSimulationCompletionException("Simulation completion requires a non-empty result snapshot");
        }
        if (annualGenerationKwh == null || annualGenerationKwh < 0) {
            throw new InvalidSimulationCompletionException("Simulation completion requires a non-negative annual generation");
        }
        if (annualSavings == null) {
            throw new InvalidSimulationCompletionException("Simulation completion requires annual savings");
        }
        if (npv == null) {
            throw new InvalidSimulationCompletionException("Simulation completion requires NPV");
        }
        if (recommendation == null || recommendation.isBlank()) {
            throw new InvalidSimulationCompletionException("Simulation completion requires a recommendation");
        }
    }
}
