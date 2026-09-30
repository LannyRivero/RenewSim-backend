package com.renewsim.backend.scenario_service.infrastructure.adapter;

import com.renewsim.backend.scenario_service.application.port.out.ScenarioTechnologyLookupPort;
import com.renewsim.backend.technology_service.application.contract.TechnologyCatalogReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TechnologyLookupJpaAdapter implements ScenarioTechnologyLookupPort {

    private final TechnologyCatalogReader technologyCatalogReader;

    @Override
    public boolean existsActiveTechnology(Long technologyId) {
        return technologyCatalogReader.existsActiveTechnology(technologyId);
    }
}
