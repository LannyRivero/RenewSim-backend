package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import com.renewsim.backend.technology_service.application.contract.TechnologyCatalogReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("simulationTechnologyLookupAdapter")
@RequiredArgsConstructor
public class TechnologyLookupJpaAdapter implements TechnologyLookupPort {

    private final TechnologyCatalogReader technologyCatalogReader;

    @Override
    public boolean existsActiveByEnergyType(String energyType) {
        return technologyCatalogReader.existsActiveByEnergyType(energyType);
    }

    @Override
    public Optional<Double> findActiveCo2ReductionFactorByEnergyType(String energyType) {
        return technologyCatalogReader.findActiveCo2ReductionFactorByEnergyType(energyType);
    }

    @Override
    public List<Long> recommendActiveTechnologyIdsByEnergyType(String energyType) {
        return technologyCatalogReader.recommendActiveTechnologyIdsByEnergyType(energyType);
    }

    @Override
    public Optional<String> findActiveEnergyTypeByTechnologyId(Long technologyId) {
        return technologyCatalogReader.findActiveEnergyTypeByTechnologyId(technologyId);
    }
}
