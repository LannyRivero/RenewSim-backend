package com.renewsim.backend.technology_service.application.port.in;

import java.util.Optional;

public interface TechnologyCatalogLookupUseCase {

    boolean existsActiveTechnology(Long technologyId);

    boolean existsActiveByEnergyType(String energyType);

    Optional<Double> findActiveCo2ReductionFactorByEnergyType(String energyType);
}
