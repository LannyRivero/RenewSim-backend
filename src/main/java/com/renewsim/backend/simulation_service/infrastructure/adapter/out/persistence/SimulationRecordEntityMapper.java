package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.SimulationStatus;
import com.renewsim.backend.simulation_service.domain.model.vo.ConsumptionProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.domain.model.vo.Currency;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectLifetime;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationEconomics;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationSystem;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationInputSnapshotCodec.SimulationInputData;
import com.renewsim.backend.simulation_service.infrastructure.persistence.entity.SimulationEntity;

/**
 * Maps between the simulation aggregate and its persistence entity.
 */
final class SimulationRecordEntityMapper {

    private final SimulationInputSnapshotCodec snapshotCodec;
    private final TechnologyLookupPort technologyLookupPort;

    SimulationRecordEntityMapper(
            SimulationInputSnapshotCodec snapshotCodec,
            TechnologyLookupPort technologyLookupPort) {
        this.snapshotCodec = snapshotCodec;
        this.technologyLookupPort = technologyLookupPort;
    }

    SimulationEntity toEntity(Simulation simulation) {
        SimulationEntity entity = new SimulationEntity();
        entity.setId(simulation.getId() == null ? null : simulation.getId().value());
        entity.setName(simulation.getName());
        entity.setEnergyType(simulation.getTechnology().value());
        entity.setLocation(simulation.getLocation().label());
        entity.setLocationLat(simulation.getLocation().lat());
        entity.setLocationLng(simulation.getLocation().lng());
        entity.setProjectSize(simulation.getSystem().installedCapacityKw());
        entity.setBudget(simulation.getEconomics().capexTotal());
        entity.setEstimatedEnergy(
                simulation.getAnnualGenerationKwh() != null ? simulation.getAnnualGenerationKwh() : 0.0);
        entity.setCo2Reduction(calculateCo2Reduction(simulation));
        entity.setCreatedBy(simulation.getCreatedBy());
        entity.setCreatedAt(simulation.getCreatedAt());
        entity.setUpdatedAt(simulation.getUpdatedAt());
        entity.setStatus(
                simulation.getStatus() != null ? simulation.getStatus().name() : SimulationStatus.DRAFT.name());
        entity.setModelVersion("solar-spain-v1");
        entity.setResourceSource("PVGIS");
        entity.setAnnualSavings(simulation.getAnnualSavings());
        entity.setNpv(simulation.getNpv());
        entity.setIrrPct(simulation.getIrrPct());
        entity.setRecommendation(simulation.getRecommendation());
        entity.setInputSnapshot(snapshotCodec.write(simulation));
        entity.setResultSnapshot(simulation.getResultSnapshot());
        return entity;
    }

    Simulation toDomain(SimulationEntity entity) {
        SimulationInputData input = snapshotCodec.readNormalized(entity.getInputSnapshot(), entity);

        SimulationLocation location = SimulationLocation.of(
                entity.getLocation(),
                entity.getLocationLat() != null ? entity.getLocationLat() : 0.0,
                entity.getLocationLng() != null ? entity.getLocationLng() : 0.0,
                input.locationCountry(),
                CountryCode.of(input.locationCountryCode()));

        SimulationSystem system = new SimulationSystem(
                entity.getProjectSize() != null ? entity.getProjectSize() : 0.0,
                input.performanceRatio(),
                input.degradationRateAnnualPct(),
                input.availabilityPct(),
                new SimulationSystem.LossesPct(
                        input.lossesInverter(), input.lossesTemperature(),
                        input.lossesWiring(), input.lossesSoiling(), input.lossesOther()));

        ConsumptionProfile demand = ConsumptionProfile.of(
                input.annualConsumptionKwh(), input.monthlyConsumptionKwh());

        SimulationEconomics economics = new SimulationEconomics(
                Currency.of(input.currency()),
                entity.getBudget() != null ? entity.getBudget() : 0.0,
                input.opexAnnual(),
                input.electricityPurchasePricePerKwh(),
                input.exportPricePerKwh(),
                input.discountRatePct(),
                ProjectLifetime.of(input.projectLifetimeYears()));

        return Simulation.reconstitute(
                entity.getId(),
                entity.getName(),
                Technology.of(entity.getEnergyType()),
                location, system, demand, economics,
                parseStatus(entity.getStatus()),
                entity.getResultSnapshot(),
                null,
                entity.getAnnualSavings(),
                entity.getNpv(),
                entity.getIrrPct(),
                entity.getRecommendation(),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private double calculateCo2Reduction(Simulation simulation) {
        if (simulation.getAnnualGenerationKwh() == null || simulation.getTechnology() == null) {
            return 0.0;
        }
        return technologyLookupPort.findActiveCo2ReductionFactorByEnergyType(simulation.getTechnology().value())
                .map(factor -> simulation.getAnnualGenerationKwh() * factor)
                .orElse(0.0);
    }

    private SimulationStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return SimulationStatus.DRAFT;
        }
        try {
            return SimulationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return SimulationStatus.DRAFT;
        }
    }
}
