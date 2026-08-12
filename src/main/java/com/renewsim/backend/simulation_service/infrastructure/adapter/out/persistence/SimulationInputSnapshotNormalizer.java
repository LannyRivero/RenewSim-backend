package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationInputSnapshotCodec.SimulationInputData;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;

import java.util.Collections;
import java.util.List;

final class SimulationInputSnapshotNormalizer {

    SimulationInputData normalize(SimulationInputData input, SimulationEntity entity) {
        String normalizedCountryCode = normalizeCountryCode(input.locationCountryCode(), entity.getLocation());
        String normalizedCountry = deriveCountry(normalizedCountryCode);

        double annualConsumption = input.annualConsumptionKwh() > 0
                ? input.annualConsumptionKwh()
                : Math.max(defaultNumber(entity.getEstimatedEnergy()), 1.0);

        List<Double> monthlyConsumption = input.monthlyConsumptionKwh() != null
                && input.monthlyConsumptionKwh().size() == 12
                        ? input.monthlyConsumptionKwh()
                        : evenMonthlyProfile(annualConsumption);

        return new SimulationInputData(
                normalizedCountry,
                normalizedCountryCode,
                input.performanceRatio() > 0 ? input.performanceRatio() : 0.81,
                input.degradationRateAnnualPct() >= 0 ? input.degradationRateAnnualPct() : 0.5,
                input.availabilityPct() > 0 ? input.availabilityPct() : 99.0,
                Math.max(input.lossesInverter(), 0.0),
                Math.max(input.lossesTemperature(), 0.0),
                Math.max(input.lossesWiring(), 0.0),
                Math.max(input.lossesSoiling(), 0.0),
                Math.max(input.lossesOther(), 0.0),
                annualConsumption,
                monthlyConsumption,
                isBlank(input.currency()) ? "EUR" : input.currency(),
                Math.max(input.opexAnnual(), 0.0),
                Math.max(input.electricityPurchasePricePerKwh(), 0.0),
                Math.max(input.exportPricePerKwh(), 0.0),
                Math.max(input.discountRatePct(), 0.0),
                input.projectLifetimeYears() >= 5 ? input.projectLifetimeYears() : 20);
    }

    private String deriveCountryCode(String locationLabel) {
        if (locationLabel != null) {
            String[] parts = locationLabel.split(",");
            String candidate = parts[parts.length - 1].trim();
            if (candidate.length() == 2) {
                return candidate.toUpperCase();
            }
        }
        return "ES";
    }

    private String normalizeCountryCode(String snapshotCountryCode, String locationLabel) {
        String candidate = isBlank(snapshotCountryCode)
                ? deriveCountryCode(locationLabel)
                : snapshotCountryCode.trim().toUpperCase();
        return CountryCode.isSupported(candidate) ? candidate : "ES";
    }

    private String deriveCountry(String countryCode) {
        return "ES".equalsIgnoreCase(countryCode) ? "Spain" : countryCode;
    }

    private List<Double> evenMonthlyProfile(double annualConsumption) {
        double monthly = annualConsumption / 12.0;
        return Collections.nCopies(12, monthly);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private double defaultNumber(Double value) {
        return value == null ? 0.0 : value;
    }
}
