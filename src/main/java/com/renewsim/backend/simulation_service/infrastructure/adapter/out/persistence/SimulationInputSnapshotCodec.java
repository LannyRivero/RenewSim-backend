package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;

import java.util.List;

/**
 * Serializes persisted simulation inputs and fills gaps from historical
 * snapshots.
 */
final class SimulationInputSnapshotCodec {

    private final ObjectMapper objectMapper;
    private final SimulationInputSnapshotNormalizer normalizer;

    SimulationInputSnapshotCodec(ObjectMapper objectMapper) {
        this(objectMapper, new SimulationInputSnapshotNormalizer());
    }

    SimulationInputSnapshotCodec(ObjectMapper objectMapper, SimulationInputSnapshotNormalizer normalizer) {
        this.objectMapper = objectMapper;
        this.normalizer = normalizer;
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
        return normalizer.normalize(read(json), entity);
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
