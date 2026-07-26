package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.application.port.out.TechnologyLookupPort;
import com.renewsim.backend.technology_service.infrastructure.persistence.entity.TechnologyEntity;
import com.renewsim.backend.technology_service.infrastructure.persistence.repository.JpaTechnologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("simulationTechnologyLookupAdapter")
@RequiredArgsConstructor
public class TechnologyLookupJpaAdapter implements TechnologyLookupPort {

    private final JpaTechnologyRepository technologyRepository;

    @Override
    public boolean existsActiveByEnergyType(String energyType) {
        TechnologyEntity.EnergyType type = parseEnergyType(energyType);
        if (type == null) {
            return false;
        }
        return technologyRepository.findByEnergyTypeAndIsActiveTrue(type, PageRequest.of(0, 1)).hasContent();
    }

    @Override
    public Optional<Double> findActiveCo2ReductionFactorByEnergyType(String energyType) {
        TechnologyEntity.EnergyType type = parseEnergyType(energyType);
        if (type == null) {
            return Optional.empty();
        }
        return technologyRepository.findFirstByEnergyTypeAndIsActiveTrueOrderByIdAsc(type)
                .map(entity -> entity.getCo2ReductionFactor().doubleValue());
    }

    private TechnologyEntity.EnergyType parseEnergyType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return TechnologyEntity.EnergyType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
