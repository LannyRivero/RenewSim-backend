package com.renewsim.backend.simulation_service.infrastructure.adapter.out.dummy;

import com.renewsim.backend.simulation_service.application.port.out.ClimateDataProviderPort;
import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import org.springframework.stereotype.Component;

@Component
public class DummyClimateDataAdapter implements ClimateDataProviderPort {

    @Override
    public ClimateData fetchClimateData(String location) {
        // Devuelve datos fijos (simulados)
        return new ClimateData(800, 5, 100);
    }
}

