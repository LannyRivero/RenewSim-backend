package com.renewsim.backend.simulation_service.application.port.out;

import java.util.List;
import com.renewsim.backend.simulation_service.domain.util.TechnologyScoringUtil.TechnologyData;

public interface TechnologyClientPort {
    List<TechnologyData> fetchTechnologiesForSimulation(Long simulationId);
}
