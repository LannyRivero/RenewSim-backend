package com.renewsim.backend.technology_service.application.dto;

public record TechnologyEstimateDTO(
    String energyType,
    double suggestedCapacityFactor,
    Double estimatedAnnualProductionKwh,
    double capacityFactorRangeMin,
    double capacityFactorRangeMax,
    String confidence
) {}
