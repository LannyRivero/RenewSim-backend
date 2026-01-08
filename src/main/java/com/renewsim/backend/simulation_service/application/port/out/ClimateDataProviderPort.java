package com.renewsim.backend.simulation_service.application.port.out;

import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;

/**
 * Outbound port for obtaining real-time climate data.
 *
 * Currently optional (frontend provides climate data).
 * Future-proof for backend recalculation of simulations.
 */
public interface ClimateDataProviderPort {
    ClimateData fetchClimateData(String location);
}
