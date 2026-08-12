package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.command.CreateSimulationFromScenarioCommand;
import com.renewsim.backend.simulation_service.domain.exception.InvalidConsumptionProfileException;
import com.renewsim.backend.simulation_service.domain.model.vo.ConsumptionProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.Currency;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectLifetime;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationEconomics;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationSystem;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import com.renewsim.backend.simulation_service.shared.application.port.out.ScenarioLookupPort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ScenarioSimulationCommandFactory {

    private static final double DEFAULT_PERFORMANCE_RATIO = 0.81;
    private static final double DEFAULT_DEGRADATION_RATE_ANNUAL_PCT = 0.5;
    private static final double DEFAULT_AVAILABILITY_PCT = 99.0;
    private static final int DEFAULT_PROJECT_LIFETIME_YEARS = 20;
    private static final double DEFAULT_OPEX_ANNUAL = 0.0;
    private static final double DEFAULT_EXPORT_PRICE_PER_KWH = 0.07;
    private static final double DEFAULT_DISCOUNT_RATE_PCT = 8.0;
    private static final String SUPPORTED_SIMULATION_CURRENCY = "EUR";

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
        return new SimulationSystem(
                scenario.defaultCapacityKw(),
                DEFAULT_PERFORMANCE_RATIO,
                DEFAULT_DEGRADATION_RATE_ANNUAL_PCT,
                DEFAULT_AVAILABILITY_PCT,
                new SimulationSystem.LossesPct(2.0, 6.0, 1.0, 3.0, 1.0));
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
        return new SimulationEconomics(
                Currency.of(resolveSimulationCurrency(scenario.defaultInvestmentCurrency())),
                scenario.defaultInvestmentAmount(),
                DEFAULT_OPEX_ANNUAL,
                scenario.defaultTariff(),
                DEFAULT_EXPORT_PRICE_PER_KWH,
                DEFAULT_DISCOUNT_RATE_PCT,
                ProjectLifetime.of(DEFAULT_PROJECT_LIFETIME_YEARS));
    }

    private String resolveSimulationCurrency(String scenarioCurrency) {
        return SUPPORTED_SIMULATION_CURRENCY;
    }
}
