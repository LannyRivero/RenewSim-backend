package com.renewsim.backend.simulation_service.shared.web;

import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.web.dto.SimulationInputSnapshotDTO;

final class SimulationInputSnapshotWebMapper {

        SimulationInputSnapshotDTO toInput(SimulationDetailsResult.Input input) {
                return new SimulationInputSnapshotDTO(
                                input.name(),
                                input.technology(),
                                new SimulationInputSnapshotDTO.LocationDTO(
                                                input.location().label(),
                                                input.location().lat(),
                                                input.location().lon(),
                                                input.location().country(),
                                                input.location().countryCode()),
                                new SimulationInputSnapshotDTO.SystemDTO(
                                                input.system().installedCapacityKw(),
                                                input.system().performanceRatio(),
                                                input.system().degradationRateAnnualPct(),
                                                input.system().availabilityPct(),
                                                new SimulationInputSnapshotDTO.LossesPctDTO(
                                                                input.system().lossesPct().inverter(),
                                                                input.system().lossesPct().temperature(),
                                                                input.system().lossesPct().wiring(),
                                                                input.system().lossesPct().soiling(),
                                                                input.system().lossesPct().other())),
                                new SimulationInputSnapshotDTO.DemandDTO(
                                                input.demand().annualConsumptionKwh(),
                                                input.demand().monthlyConsumptionKwh()),
                                new SimulationInputSnapshotDTO.EconomicsDTO(
                                                input.economics().currency(),
                                                input.economics().capexTotal(),
                                                input.economics().opexAnnual(),
                                                input.economics().electricityPurchasePricePerKwh(),
                                                input.economics().exportPricePerKwh(),
                                                input.economics().discountRatePct(),
                                                input.economics().projectLifetimeYears()));
        }
}
