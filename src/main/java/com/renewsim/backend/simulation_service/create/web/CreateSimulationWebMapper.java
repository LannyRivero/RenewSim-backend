package com.renewsim.backend.simulation_service.create.web;

import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.domain.model.vo.ConsumptionProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.domain.model.vo.Currency;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectLifetime;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationEconomics;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationSystem;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import com.renewsim.backend.simulation_service.create.web.dto.CreateSimulationRequestDTO;

import java.util.List;

public final class CreateSimulationWebMapper {

        public CreateRealSimulationCommand toCommand(CreateSimulationRequestDTO request, String username) {
                return new CreateRealSimulationCommand(
                                request.name(),
                                Technology.of(request.energyType()),
                                SimulationLocation.of(
                                                request.location().label(),
                                                request.location().lat(),
                                                request.location().lon(),
                                                request.location().country(),
                                                CountryCode.of(request.location().countryCode())),
                                new SimulationSystem(
                                                request.system().installedCapacityKw(),
                                                request.system().performanceRatio(),
                                                request.system().degradationRateAnnualPct(),
                                                request.system().availabilityPct(),
                                                new SimulationSystem.LossesPct(
                                                                request.system().lossesPct().inverter(),
                                                                request.system().lossesPct().temperature(),
                                                                request.system().lossesPct().wiring(),
                                                                request.system().lossesPct().soiling(),
                                                                request.system().lossesPct().other())),
                                ConsumptionProfile.of(
                                                request.demand().annualConsumptionKwh(),
                                                request.demand().monthlyConsumptionKwh()),
                                new SimulationEconomics(
                                                Currency.of(request.economics().currency()),
                                                request.economics().capexTotal(),
                                                request.economics().opexAnnual(),
                                                request.economics().electricityPurchasePricePerKwh(),
                                                request.economics().exportPricePerKwh(),
                                                request.economics().discountRatePct(),
                                                ProjectLifetime.of(request.economics().projectLifetimeYears())),
                                request.technologyIds() == null ? List.of() : List.copyOf(request.technologyIds()),
                                null,
                                username);
        }
}
