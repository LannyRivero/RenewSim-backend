package com.renewsim.backend.simulation_service.create.application.technology;

import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.FinancialCalculator;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;

import java.util.List;

public interface SimulationAssessmentPolicy {
    boolean supports(Technology technology);

    Assessment assess(CreateRealSimulationCommand command, TechnologySystemProfile systemProfile, double primaryYieldMetric, FinancialCalculator.Result financialResult);

    List<SimulationDetailsResult.SimulationWarning> buildWarnings(CreateRealSimulationCommand command, TechnologySystemProfile systemProfile);

    record Assessment(String recommendation, String headline, String summary,
                      List<SimulationDetailsResult.RecommendationReason> reasons) {
    }
}
