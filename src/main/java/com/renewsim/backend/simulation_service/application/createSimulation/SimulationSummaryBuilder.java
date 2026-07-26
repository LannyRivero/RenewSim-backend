package com.renewsim.backend.simulation_service.application.createSimulation;

import com.renewsim.backend.simulation_service.application.shared.SimulationDetailsResult;

import java.util.ArrayList;
import java.util.List;

public final class SimulationSummaryBuilder {

    private SimulationSummaryBuilder() {
    }

    public record Result(String recommendation, String headline, String summary,
            List<SimulationDetailsResult.RecommendationReason> reasons) {
    }

    public static Result build(
            CreateRealSimulationCommand command,
            double specificYield,
            FinancialCalculator.Result financialResult) {

        List<SimulationDetailsResult.RecommendationReason> reasons = new ArrayList<>();

        if (specificYield >= 1400) {
            reasons.add(new SimulationDetailsResult.RecommendationReason("resource", "positive",
                    "The selected site shows strong solar resource for the modeled system."));
        } else if (specificYield >= 1100) {
            reasons.add(new SimulationDetailsResult.RecommendationReason("resource", "warning",
                    "The selected site is viable, but solar yield is moderate versus top Spanish locations."));
        } else {
            reasons.add(new SimulationDetailsResult.RecommendationReason("resource", "critical",
                    "The modeled solar yield is weak for the selected assumptions and location."));
        }

        if (financialResult.npv() > 0 && financialResult.irrPct() != null
                && financialResult.irrPct() >= command.economics().discountRatePct()) {
            reasons.add(new SimulationDetailsResult.RecommendationReason("economics", "positive",
                    "Discounted value creation remains positive under the submitted assumptions."));
        } else if (financialResult.paybackYears() != null
                && financialResult.paybackYears() <= command.economics().projectLifetime().years()) {
            reasons.add(new SimulationDetailsResult.RecommendationReason("economics", "warning",
                    "Recovery is feasible, but discounted performance should be reviewed carefully."));
        } else {
            reasons.add(new SimulationDetailsResult.RecommendationReason("economics", "critical",
                    "The submitted assumptions do not recover investment adequately within project life."));
        }

        double totalLossPct = command.system().lossesPct().inverter()
                + command.system().lossesPct().temperature()
                + command.system().lossesPct().wiring()
                + command.system().lossesPct().soiling()
                + command.system().lossesPct().other();

        if (command.system().performanceRatio() < 0.78 || totalLossPct > 20.0) {
            reasons.add(new SimulationDetailsResult.RecommendationReason("assumptions", "warning",
                    "Performance assumptions are conservative and materially affect the outcome."));
        }

        String recommendation;
        String headline;
        String summary;

        if (financialResult.npv() > 0
                && financialResult.irrPct() != null
                && financialResult.irrPct() >= command.economics().discountRatePct()
                && financialResult.paybackYears() != null
                && financialResult.paybackYears() <= command.economics().projectLifetime().years() / 2.0
                && specificYield >= 1250) {
            recommendation = "recommended";
            headline = "The scenario is technically solid and clears the baseline financial gate.";
            summary = "Resource quality, annual yield, and discounted returns support moving the case into detailed engineering and commercial validation.";
        } else if (financialResult.npv() > 0
                || (financialResult.paybackYears() != null
                        && financialResult.paybackYears() <= command.economics().projectLifetime().years())) {
            recommendation = "viable_with_reservations";
            headline = "The scenario is viable, but the decision depends on validating core assumptions.";
            summary = "The project shows credible technical output, while the financial profile still requires executive review of pricing, losses, and recovery targets.";
        } else {
            recommendation = "not_recommended";
            headline = "The scenario should not move forward without material assumption changes.";
            summary = "Under the submitted inputs, the technical and financial outputs do not justify progressing the case in its current form.";
        }

        return new Result(recommendation, headline, summary, reasons);
    }

    public static List<SimulationDetailsResult.SimulationWarning> buildWarnings(
            CreateRealSimulationCommand command) {

        List<SimulationDetailsResult.SimulationWarning> warnings = new ArrayList<>();
        warnings.add(new SimulationDetailsResult.SimulationWarning("info",
                "MONTHLY_PROFILE_USER_SUPPLIED",
                "Monthly demand profile was supplied by the user and accepted without normalization."));

        if (command.system().availabilityPct() < 95.0) {
            warnings.add(new SimulationDetailsResult.SimulationWarning("warning",
                    "LOW_AVAILABILITY_ASSUMPTION",
                    "Availability assumption is below 95% and may materially reduce annual output."));
        }

        double totalLossPct = command.system().lossesPct().inverter()
                + command.system().lossesPct().temperature()
                + command.system().lossesPct().wiring()
                + command.system().lossesPct().soiling()
                + command.system().lossesPct().other();

        if (totalLossPct > 20.0) {
            warnings.add(new SimulationDetailsResult.SimulationWarning("warning",
                    "HIGH_SYSTEM_LOSSES",
                    "Combined system losses exceed 20% and should be validated before investment review."));
        }
        return warnings;
    }
}
