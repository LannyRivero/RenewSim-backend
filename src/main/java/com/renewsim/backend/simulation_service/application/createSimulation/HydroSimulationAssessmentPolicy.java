package com.renewsim.backend.simulation_service.application.createSimulation;

import com.renewsim.backend.simulation_service.application.shared.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HydroSimulationAssessmentPolicy implements SimulationAssessmentPolicy {

    @Override
    public boolean supports(Technology technology) {
        return "hydro".equals(technology.value());
    }

    @Override
    public Assessment assess(CreateRealSimulationCommand command, TechnologySystemProfile systemProfile, double primaryYieldMetric, FinancialCalculator.Result financialResult) {
        throw new IllegalStateException("Hydro simulation assessment should not be used before hydro simulation is implemented.");
    }

    @Override
    public List<SimulationDetailsResult.SimulationWarning> buildWarnings(CreateRealSimulationCommand command, TechnologySystemProfile systemProfile) {
        throw new IllegalStateException("Hydro simulation assessment should not be used before hydro simulation is implemented.");
    }
}
