package com.renewsim.backend.simulation_service.create.application.technology.solar;

import com.renewsim.backend.simulation_service.create.application.technology.TechnologySystemProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationSystem;

public record SolarSystemProfile(
        double installedCapacityKw,
        double performanceRatio,
        double degradationRateAnnualPct,
        double availabilityPct,
        Losses losses) implements TechnologySystemProfile {

    public static SolarSystemProfile from(SimulationSystem system) {
        return new SolarSystemProfile(
                system.installedCapacityKw(),
                system.performanceRatio(),
                system.degradationRateAnnualPct(),
                system.availabilityPct(),
                new Losses(
                        system.lossesPct().inverter(),
                        system.lossesPct().temperature(),
                        system.lossesPct().wiring(),
                        system.lossesPct().soiling(),
                        system.lossesPct().other()));
    }

    public record Losses(
            double inverter,
            double temperature,
            double wiring,
            double soiling,
            double other) {

        public double total() {
            return inverter + temperature + wiring + soiling + other;
        }
    }
}
