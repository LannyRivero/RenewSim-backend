package com.renewsim.backend.simulation_service.domain.model.vo;

import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationEconomicsException;

public record SimulationEconomics(
        Currency currency,
        double capexTotal,
        double opexAnnual,
        double electricityPurchasePricePerKwh,
        double exportPricePerKwh,
        double discountRatePct,
        ProjectLifetime projectLifetime) {

    public SimulationEconomics {
        if (currency == null) {
            throw new InvalidSimulationEconomicsException("VALIDATION_ERROR: currency is required");
        }
        if (capexTotal < 0) {
            throw new InvalidSimulationEconomicsException("VALIDATION_ERROR: capexTotal must be greater than or equal to 0");
        }
        if (opexAnnual < 0) {
            throw new InvalidSimulationEconomicsException("VALIDATION_ERROR: opexAnnual must be greater than or equal to 0");
        }
        if (electricityPurchasePricePerKwh < 0) {
            throw new InvalidSimulationEconomicsException("VALIDATION_ERROR: electricityPurchasePricePerKwh must be greater than or equal to 0");
        }
        if (exportPricePerKwh < 0) {
            throw new InvalidSimulationEconomicsException("VALIDATION_ERROR: exportPricePerKwh must be greater than or equal to 0");
        }
        if (discountRatePct < 0) {
            throw new InvalidSimulationEconomicsException("VALIDATION_ERROR: discountRatePct must be greater than or equal to 0");
        }
        if (projectLifetime == null) {
            throw new InvalidSimulationEconomicsException("VALIDATION_ERROR: projectLifetime is required");
        }
    }
}
