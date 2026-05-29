package com.renewsim.backend.simulation_service.infrastructure.adapter.out.external;

import com.renewsim.backend.simulation_service.application.port.out.TechnologyRecommendationPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.technology_service.application.service.TechnologyRecommenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TechnologyRecommendationAdapter implements TechnologyRecommendationPort {

    private final TechnologyRecommenderService technologyRecommenderService;

    @Override
    public List<Long> recommendFor(Simulation simulation) {
        return technologyRecommenderService.recommendFor(simulation);
    }
}
