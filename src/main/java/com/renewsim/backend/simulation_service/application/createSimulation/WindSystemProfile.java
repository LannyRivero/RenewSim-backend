package com.renewsim.backend.simulation_service.application.createSimulation;

public record WindSystemProfile(
        double installedCapacityKw,
        double degradationRateAnnualPct,
        double availabilityPct) implements TechnologySystemProfile {
}
