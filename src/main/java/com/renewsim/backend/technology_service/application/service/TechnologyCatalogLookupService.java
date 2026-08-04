package com.renewsim.backend.technology_service.application.service;

import com.renewsim.backend.technology_service.application.port.in.TechnologyCatalogLookupUseCase;
import com.renewsim.backend.technology_service.application.port.out.TechnologyRepositoryPort;
import com.renewsim.backend.technology_service.domain.model.vo.EnergyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TechnologyCatalogLookupService implements TechnologyCatalogLookupUseCase {

    private final TechnologyRepositoryPort technologyRepository;

    @Override
    public boolean existsActiveTechnology(Long technologyId) {
        return technologyRepository.findActiveById(technologyId).isPresent();
    }

    @Override
    public boolean existsActiveByEnergyType(String energyType) {
        EnergyType parsed = parseEnergyType(energyType);
        if (parsed == null) {
            return false;
        }
        return technologyRepository.findFirstActiveByEnergyType(parsed).isPresent();
    }

    @Override
    public Optional<Double> findActiveCo2ReductionFactorByEnergyType(String energyType) {
        EnergyType parsed = parseEnergyType(energyType);
        if (parsed == null) {
            return Optional.empty();
        }
        return technologyRepository.findFirstActiveByEnergyType(parsed)
                .map(technology -> technology.getCo2Reduction().value().doubleValue());
    }

    private EnergyType parseEnergyType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return EnergyType.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
