package com.renewsim.backend.scenario_service.infrastructure.adapter;

import com.renewsim.backend.scenario_service.application.port.out.ScenarioTechnologyLookupPort;
import com.renewsim.backend.technology_service.infrastructure.persistence.repository.JpaTechnologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TechnologyLookupJpaAdapter implements ScenarioTechnologyLookupPort {

    private final JpaTechnologyRepository technologyRepository;

    @Override
    public boolean existsActiveTechnology(Long technologyId) {
        return technologyRepository.findByIdAndIsActiveTrue(technologyId).isPresent();
    }
}
