package com.renewsim.backend.technology_service.application.port.in;

import java.util.List;
import java.util.Optional;

public interface TechnologyCatalogLookupUseCase {

    boolean existsActiveTechnology(Long technologyId);

    boolean existsActiveByEnergyType(String energyType);

    Optional<Double> findActiveCo2ReductionFactorByEnergyType(String energyType);

    List<Long> recommendActiveTechnologyIdsByEnergyType(String energyType);

    Optional<String> findActiveEnergyTypeByTechnologyId(Long technologyId);
}
