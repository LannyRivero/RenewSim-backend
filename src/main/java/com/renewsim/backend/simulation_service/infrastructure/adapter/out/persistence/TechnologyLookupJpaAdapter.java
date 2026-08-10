package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import com.renewsim.backend.technology_service.application.port.in.TechnologyCatalogLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("simulationTechnologyLookupAdapter")
@RequiredArgsConstructor
public class TechnologyLookupJpaAdapter implements TechnologyLookupPort {

    private final TechnologyCatalogLookupUseCase technologyCatalogLookupUseCase;

    @Override
    public boolean existsActiveByEnergyType(String energyType) {
        return technologyCatalogLookupUseCase.existsActiveByEnergyType(energyType);
    }

    @Override
    public Optional<Double> findActiveCo2ReductionFactorByEnergyType(String energyType) {
        return technologyCatalogLookupUseCase.findActiveCo2ReductionFactorByEnergyType(energyType);
    }

    @Override
    public List<Long> recommendActiveTechnologyIdsByEnergyType(String energyType) {
        return technologyCatalogLookupUseCase.recommendActiveTechnologyIdsByEnergyType(energyType);
    }

    @Override
    public Optional<String> findActiveEnergyTypeByTechnologyId(Long technologyId) {
        return technologyCatalogLookupUseCase.findActiveEnergyTypeByTechnologyId(technologyId);
    }
}
