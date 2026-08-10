package com.renewsim.backend.simulation_service.shared.application.port.out;

import java.util.List;
import java.util.Optional;

public interface TechnologyLookupPort {

    boolean existsActiveByEnergyType(String energyType);

    Optional<Double> findActiveCo2ReductionFactorByEnergyType(String energyType);

    List<Long> recommendActiveTechnologyIdsByEnergyType(String energyType);

    Optional<String> findActiveEnergyTypeByTechnologyId(Long technologyId);
}
