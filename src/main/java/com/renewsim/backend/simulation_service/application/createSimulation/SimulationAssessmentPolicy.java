package com.renewsim.backend.simulation_service.application.createSimulation;

import com.renewsim.backend.simulation_service.application.shared.SimulationDetailsResult;
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
