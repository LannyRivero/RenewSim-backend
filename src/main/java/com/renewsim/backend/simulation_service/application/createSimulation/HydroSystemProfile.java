package com.renewsim.backend.simulation_service.application.createSimulation;

public record HydroSystemProfile(
        double installedCapacityKw,
        double degradationRateAnnualPct,
        double availabilityPct) implements TechnologySystemProfile {
}
