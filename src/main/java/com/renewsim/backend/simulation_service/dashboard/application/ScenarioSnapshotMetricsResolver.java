package com.renewsim.backend.simulation_service.dashboard.application;

import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class ScenarioSnapshotMetricsResolver {

    private final TechnologyLookupPort technologyLookupPort;

    public ScenarioSnapshotMetrics resolve(Simulation simulation, SimulationDetailsResult details) {
        SimulationDetailsResult.Financial financial = details != null ? details.financial() : null;
        SimulationDetailsResult.Technical technical = details != null ? details.technical() : null;

        Double capex = simulation.getEconomics() != null ? simulation.getEconomics().capexTotal() : null;
        Double annualSavings = financial != null ? Double.valueOf(financial.annualSavings())
                : simulation.getAnnualSavings();
        Double annualBenefit = financial != null ? Double.valueOf(financial.netAnnualBenefit())
                : simulation.getAnnualSavings();
        Double paybackYears = financial != null ? financial.paybackYears() : null;
        Double roiPercent = roiPercent(capex, annualBenefit);
        double annualGeneration = technical != null ? technical.annualGenerationKwh()
                : defaultNumber(simulation.getAnnualGenerationKwh());
        double co2SavedKg = annualGeneration > 0.0 && simulation.getTechnology() != null
                ? technologyLookupPort.findActiveCo2ReductionFactorByEnergyType(simulation.getTechnology().value())
                        .map(factor -> annualGeneration * factor)
                        .orElse(0.0)
                : 0.0;

        return new ScenarioSnapshotMetrics(capex, annualSavings, annualBenefit, paybackYears, roiPercent,
                annualGeneration, co2SavedKg);
    }

    private Double roiPercent(Double capex, Double annualBenefit) {
        if (capex == null || annualBenefit == null || capex <= 0.0) {
            return null;
        }
        return round((annualBenefit / capex) * 100.0);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double defaultNumber(Double value) {
        return value == null ? 0.0 : value;
    }

    public record ScenarioSnapshotMetrics(
            Double capex,
            Double annualSavings,
            Double annualBenefit,
            Double paybackYears,
            Double roiPercent,
            double annualGeneration,
            double co2SavedKg) {
    }
}
