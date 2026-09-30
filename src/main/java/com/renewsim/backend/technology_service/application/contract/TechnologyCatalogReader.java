package com.renewsim.backend.technology_service.application.contract;

import java.util.List;
import java.util.Optional;

/**
 * Read-only catalog contract exposed by technology_service for cross-context consumers.
 */
public interface TechnologyCatalogReader {

    boolean existsActiveTechnology(Long technologyId);

    boolean existsActiveByEnergyType(String energyType);

    Optional<Double> findActiveCo2ReductionFactorByEnergyType(String energyType);

    List<Long> recommendActiveTechnologyIdsByEnergyType(String energyType);

    Optional<String> findActiveEnergyTypeByTechnologyId(Long technologyId);
}
