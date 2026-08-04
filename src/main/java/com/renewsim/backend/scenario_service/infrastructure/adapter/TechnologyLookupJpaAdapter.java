package com.renewsim.backend.scenario_service.infrastructure.adapter;

import com.renewsim.backend.scenario_service.application.port.out.ScenarioTechnologyLookupPort;
import com.renewsim.backend.technology_service.application.port.in.TechnologyCatalogLookupUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TechnologyLookupJpaAdapter implements ScenarioTechnologyLookupPort {

    private final TechnologyCatalogLookupUseCase technologyCatalogLookupUseCase;

    @Override
    public boolean existsActiveTechnology(Long technologyId) {
        return technologyCatalogLookupUseCase.existsActiveTechnology(technologyId);
    }
}
