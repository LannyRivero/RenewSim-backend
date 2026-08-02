package com.renewsim.backend.simulation_service.application.createSimulation;

import com.renewsim.backend.simulation_service.application.shared.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.create.application.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.FinancialCalculator;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WindSimulationAssessmentPolicy implements SimulationAssessmentPolicy {

    @Override
    public boolean supports(Technology technology) {
        return "wind".equals(technology.value());
    }

    @Override
    public Assessment assess(CreateRealSimulationCommand command, TechnologySystemProfile systemProfile, double primaryYieldMetric, FinancialCalculator.Result financialResult) {
        throw new IllegalStateException("Wind simulation assessment should not be used before wind simulation is implemented.");
    }

    @Override
    public List<SimulationDetailsResult.SimulationWarning> buildWarnings(CreateRealSimulationCommand command, TechnologySystemProfile systemProfile) {
        throw new IllegalStateException("Wind simulation assessment should not be used before wind simulation is implemented.");
    }
}
