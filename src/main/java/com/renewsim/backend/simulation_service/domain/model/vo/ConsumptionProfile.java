package com.renewsim.backend.simulation_service.domain.model.vo;

import com.renewsim.backend.simulation_service.domain.exception.InvalidConsumptionProfileException;

import java.util.List;

public record ConsumptionProfile(double annualConsumptionKwh, List<Double> monthlyConsumptionKwh) {

    public static ConsumptionProfile of(double annualConsumptionKwh, List<Double> monthlyConsumptionKwh) {
        return new ConsumptionProfile(annualConsumptionKwh, monthlyConsumptionKwh);
    }

    public ConsumptionProfile {
        if (annualConsumptionKwh <= 0) {
            throw new InvalidConsumptionProfileException("VALIDATION_ERROR: annualConsumptionKwh must be positive");
        }
        if (monthlyConsumptionKwh == null || monthlyConsumptionKwh.size() != 12) {
            throw new InvalidConsumptionProfileException("VALIDATION_ERROR: monthlyConsumptionKwh must have exactly 12 values");
        }
        double monthlySum = monthlyConsumptionKwh.stream().mapToDouble(Double::doubleValue).sum();
        double tolerance = Math.max(annualConsumptionKwh * 0.02, 1.0);
        if (Math.abs(monthlySum - annualConsumptionKwh) > tolerance) {
            throw new InvalidConsumptionProfileException("VALIDATION_ERROR: annualConsumptionKwh must approximately match the monthly sum");
        }
    }
}
