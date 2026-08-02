package com.renewsim.backend.simulation_service.create.application.technology.wind;

import com.renewsim.backend.simulation_service.create.application.technology.TechnologySystemProfile;

public record WindSystemProfile(
        double installedCapacityKw,
        double degradationRateAnnualPct,
        double availabilityPct) implements TechnologySystemProfile {
}
