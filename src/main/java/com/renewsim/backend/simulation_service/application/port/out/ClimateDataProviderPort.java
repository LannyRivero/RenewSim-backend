package com.renewsim.backend.simulation_service.application.port.out;

import com.renewsim.backend.simulation_service.domain.model.vo.ClimateData;
import com.renewsim.backend.simulation_service.domain.model.vo.ResolvedLocation;

import java.util.List;

/**
 * Outbound port for obtaining climate data from coordinates.
 *
 * Currently used by backend so frontend does not calculate climate metrics.
 */
public interface ClimateDataProviderPort {
    ClimateData fetchClimateData(double latitude, double longitude);

    ResolvedLocation resolveLocation(double latitude, double longitude);

    List<ResolvedLocation> searchLocations(String query, int limit);
}