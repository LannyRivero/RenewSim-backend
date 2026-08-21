package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.command.CreateSimulationFromScenarioCommand;
import com.renewsim.backend.simulation_service.domain.exception.InvalidConsumptionProfileException;
import com.renewsim.backend.simulation_service.domain.model.vo.ConsumptionProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationEconomics;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationSystem;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import com.renewsim.backend.simulation_service.shared.application.port.out.ScenarioLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScenarioSimulationCommandFactory {

    private final ScenarioSimulationDefaultsPolicy defaultsPolicy;

    public CreateRealSimulationCommand fromScenario(
            CreateSimulationFromScenarioCommand command,
            ScenarioLookupPort.ScenarioSnapshot scenario,
            String energyType,
            List<Long> technologyIds) {
        return new CreateRealSimulationCommand(
                resolveSimulationName(command.name(), scenario.name()),
                Technology.of(energyType),
                command.location(),
                buildSystem(scenario),
                buildDemand(scenario),
                buildEconomics(scenario),
                technologyIds,
                scenario.id(),
                command.createdBy());
    }

    private String resolveSimulationName(String requestedName, String scenarioName) {
        return requestedName == null || requestedName.isBlank() ? scenarioName : requestedName;
    }

    private SimulationSystem buildSystem(ScenarioLookupPort.ScenarioSnapshot scenario) {
        return defaultsPolicy.buildSystem(scenario.defaultCapacityKw());
    }

    private ConsumptionProfile buildDemand(ScenarioLookupPort.ScenarioSnapshot scenario) {
        if (scenario.defaultConsumption() <= 0) {
            throw new InvalidConsumptionProfileException(
                    "VALIDATION_ERROR: scenario defaultConsumption must be positive");
        }
        double monthly = scenario.defaultConsumption() / 12.0;
        List<Double> monthlyConsumption = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            monthlyConsumption.add(monthly);
        }
        return ConsumptionProfile.of(scenario.defaultConsumption(), monthlyConsumption);
    }

    private SimulationEconomics buildEconomics(ScenarioLookupPort.ScenarioSnapshot scenario) {
        return defaultsPolicy.buildEconomics(
                scenario.defaultInvestmentAmount(),
                scenario.defaultInvestmentCurrency(),
                scenario.defaultTariff());
    }
}
