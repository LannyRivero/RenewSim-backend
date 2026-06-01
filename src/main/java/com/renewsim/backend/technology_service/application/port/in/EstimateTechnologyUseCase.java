package com.renewsim.backend.technology_service.application.port.in;

import com.renewsim.backend.technology_service.application.dto.TechnologyEstimateDTO;

public interface EstimateTechnologyUseCase {
    TechnologyEstimateDTO estimate(String energyType, Double installedCapacityKw);
}
