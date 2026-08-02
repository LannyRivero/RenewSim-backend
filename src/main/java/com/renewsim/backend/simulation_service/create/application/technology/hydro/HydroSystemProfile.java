package com.renewsim.backend.simulation_service.create.application.technology.hydro;

import com.renewsim.backend.simulation_service.create.application.technology.TechnologySystemProfile;

public record HydroSystemProfile(
        double installedCapacityKw,
        double degradationRateAnnualPct,
        double availabilityPct) implements TechnologySystemProfile {
}
