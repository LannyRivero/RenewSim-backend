package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;

import java.util.Collections;
import java.util.List;

/**
 * Serializes persisted simulation inputs and fills gaps from historical
 * snapshots.
 */
final class SimulationInputSnapshotCodec {

    private final ObjectMapper objectMapper;

    SimulationInputSnapshotCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    String write(Simulation simulation) {
        try {
            SimulationInputData data = new SimulationInputData(
                    simulation.getLocation().country(),
                    simulation.getLocation().countryCode().value(),
                    simulation.getSystem().performanceRatio(),
                    simulation.getSystem().degradationRateAnnualPct(),
                    simulation.getSystem().availabilityPct(),
                    simulation.getSystem().lossesPct().inverter(),
                    simulation.getSystem().lossesPct().temperature(),
                    simulation.getSystem().lossesPct().wiring(),
                    simulation.getSystem().lossesPct().soiling(),
                    simulation.getSystem().lossesPct().other(),
                    simulation.getDemand().annualConsumptionKwh(),
                    simulation.getDemand().monthlyConsumptionKwh(),
                    simulation.getEconomics().currency().value(),
                    simulation.getEconomics().opexAnnual(),
                    simulation.getEconomics().electricityPurchasePricePerKwh(),
                    simulation.getEconomics().exportPricePerKwh(),
                    simulation.getEconomics().discountRatePct(),
                    simulation.getEconomics().projectLifetime().years());
            return objectMapper.writeValueAsString(data);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize input snapshot", ex);
        }
    }

    SimulationInputData readNormalized(String json, SimulationEntity entity) {
        SimulationInputData input = read(json);
        String normalizedCountryCode = isBlank(input.locationCountryCode())
                ? deriveCountryCode(entity.getLocation())
                : input.locationCountryCode();
        String normalizedCountry = isBlank(input.locationCountry())
                ? deriveCountry(normalizedCountryCode)
                : input.locationCountry();

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

    private SimulationInputData read(String json) {
        if (json == null || json.isBlank()) {
            return SimulationInputData.empty();
        }
        try {
            return objectMapper.readValue(json, SimulationInputData.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize input snapshot", ex);
        }
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

    record SimulationInputData(
            String locationCountry,
            String locationCountryCode,
            double performanceRatio,
            double degradationRateAnnualPct,
            double availabilityPct,
            double lossesInverter,
            double lossesTemperature,
            double lossesWiring,
            double lossesSoiling,
            double lossesOther,
            double annualConsumptionKwh,
            List<Double> monthlyConsumptionKwh,
            String currency,
            double opexAnnual,
            double electricityPurchasePricePerKwh,
            double exportPricePerKwh,
            double discountRatePct,
            int projectLifetimeYears) {

        static SimulationInputData empty() {
            return new SimulationInputData(
                    "", "", 0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0, 0.0, 0.0,
                    0.0, List.of(),
                    "EUR", 0.0, 0.0, 0.0, 0.0, 25);
        }
    }
}
