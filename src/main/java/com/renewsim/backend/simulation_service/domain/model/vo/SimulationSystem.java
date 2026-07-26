package com.renewsim.backend.simulation_service.domain.model.vo;

import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationSystemException;

public record SimulationSystem(
        double installedCapacityKw,
        double performanceRatio,
        double degradationRateAnnualPct,
        double availabilityPct,
        LossesPct lossesPct) {

    public SimulationSystem {
        if (installedCapacityKw <= 0) {
            throw new InvalidSimulationSystemException("VALIDATION_ERROR: installedCapacityKw must be positive");
        }
        if (performanceRatio <= 0 || performanceRatio > 1) {
            throw new InvalidSimulationSystemException("VALIDATION_ERROR: performanceRatio must be greater than 0 and less than or equal to 1");
        }
        if (degradationRateAnnualPct < 0 || degradationRateAnnualPct > 5) {
            throw new InvalidSimulationSystemException("VALIDATION_ERROR: degradationRateAnnualPct must be between 0 and 5");
        }
        if (availabilityPct <= 0 || availabilityPct > 100) {
            throw new InvalidSimulationSystemException("VALIDATION_ERROR: availabilityPct must be greater than 0 and less than or equal to 100");
        }
        if (lossesPct == null) {
            throw new InvalidSimulationSystemException("VALIDATION_ERROR: lossesPct is required");
        }
    }

    public record LossesPct(
            double inverter,
            double temperature,
            double wiring,
            double soiling,
            double other) {

        public LossesPct {
            if (inverter < 0 || temperature < 0 || wiring < 0 || soiling < 0 || other < 0) {
                throw new InvalidSimulationSystemException("VALIDATION_ERROR: all loss percentages must be greater than or equal to 0");
            }
        }

        public double total() {
            return inverter + temperature + wiring + soiling + other;
        }
    }
}
