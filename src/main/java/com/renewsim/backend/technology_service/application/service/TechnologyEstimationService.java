package com.renewsim.backend.technology_service.application.service;

import com.renewsim.backend.shared.exception.BadRequestException;
import com.renewsim.backend.technology_service.application.dto.TechnologyEstimateDTO;
import com.renewsim.backend.technology_service.application.port.in.EstimateTechnologyUseCase;
import com.renewsim.backend.technology_service.domain.model.vo.EnergyType;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TechnologyEstimationService implements EstimateTechnologyUseCase {

    private static final double HOURS_PER_YEAR = 8760.0;

    private static final Map<String, CapacityFactorRange> DEFAULT_CAPACITY_FACTORS = Map.of(
            "SOLAR", new CapacityFactorRange(12.0, 25.0, 18.0),
            "WIND", new CapacityFactorRange(20.0, 45.0, 30.0),
            "HYDRO", new CapacityFactorRange(30.0, 70.0, 50.0),
            "GEOTHERMAL", new CapacityFactorRange(60.0, 90.0, 75.0),
            "BIOMASS", new CapacityFactorRange(50.0, 80.0, 65.0));

    @Override
    public TechnologyEstimateDTO estimate(String energyType, Double installedCapacityKw) {
        String normalized = normalizeEnergyType(energyType);
        CapacityFactorRange range = DEFAULT_CAPACITY_FACTORS.getOrDefault(normalized,
                new CapacityFactorRange(10.0, 50.0, 25.0));

        Double annualProduction = null;
        String confidence = "medium";

        if (installedCapacityKw != null && installedCapacityKw > 0) {
            annualProduction = installedCapacityKw * (range.defaultFactor() / 100.0) * HOURS_PER_YEAR;
            confidence = annualProduction > 0 ? "high" : "low";
        }

        return new TechnologyEstimateDTO(
                normalized,
                range.defaultFactor(),
                annualProduction,
                range.min(),
                range.max(),
                confidence);
    }

    private String normalizeEnergyType(String energyType) {
        try {
            return EnergyType.fromString(energyType).name();
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid energy type: " + energyType);
        }
    }

    private record CapacityFactorRange(double min, double max, double defaultFactor) {
    }
}
