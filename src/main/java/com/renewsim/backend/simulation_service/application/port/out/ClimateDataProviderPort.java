package com.renewsim.backend.simulation_service.application.port.out;

import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;

public interface ClimateDataProviderPort {
    ClimateData fetchClimateData(String location);
}

