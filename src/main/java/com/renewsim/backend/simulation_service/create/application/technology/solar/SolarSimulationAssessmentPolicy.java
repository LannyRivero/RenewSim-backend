package com.renewsim.backend.simulation_service.create.application.technology.solar;

import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.FinancialCalculator;
import com.renewsim.backend.simulation_service.create.application.technology.SimulationAssessmentPolicy;
import com.renewsim.backend.simulation_service.create.application.technology.TechnologySystemProfile;
import com.renewsim.backend.simulation_service.domain.policy.SolarSimulationRecommendationPolicy;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Primary
public class SolarSimulationAssessmentPolicy implements SimulationAssessmentPolicy {

    private final SolarSimulationRecommendationPolicy recommendationPolicy = new SolarSimulationRecommendationPolicy();

    @Override
    public boolean supports(Technology technology) {
        return "solar".equals(technology.value());
    }

    @Override
    public Assessment assess(
            CreateRealSimulationCommand command,
            TechnologySystemProfile systemProfile,
            double specificYield,
            FinancialCalculator.Result financialResult) {
        SolarSystemProfile solarProfile = requireSolarProfile(systemProfile);

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

        double totalLossPct = solarProfile.losses().total();

        if (solarProfile.performanceRatio() < 0.78 || totalLossPct > 20.0) {
            reasons.add(new SimulationDetailsResult.RecommendationReason("assumptions", "warning",
                    "Performance assumptions are conservative and materially affect the outcome."));
        }

        SolarSimulationRecommendationPolicy.RecommendationDecision decision = recommendationPolicy.decide(
                financialResult.npv(),
                financialResult.irrPct(),
                command.economics().discountRatePct(),
                financialResult.paybackYears(),
                command.economics().projectLifetime().years(),
                specificYield);

        return new Assessment(decision.recommendation().wireValue(), decision.headline(), decision.summary(), reasons);
    }

    @Override
    public List<SimulationDetailsResult.SimulationWarning> buildWarnings(CreateRealSimulationCommand command, TechnologySystemProfile systemProfile) {
        SolarSystemProfile solarProfile = requireSolarProfile(systemProfile);
        List<SimulationDetailsResult.SimulationWarning> warnings = new ArrayList<>();
        warnings.add(new SimulationDetailsResult.SimulationWarning("info",
                "MONTHLY_PROFILE_USER_SUPPLIED",
                "Monthly demand profile was supplied by the user and accepted without normalization."));

        if (solarProfile.availabilityPct() < 95.0) {
            warnings.add(new SimulationDetailsResult.SimulationWarning("warning",
                    "LOW_AVAILABILITY_ASSUMPTION",
                    "Availability assumption is below 95% and may materially reduce annual output."));
        }

        double totalLossPct = solarProfile.losses().total();

        if (totalLossPct > 20.0) {
            warnings.add(new SimulationDetailsResult.SimulationWarning("warning",
                    "HIGH_SYSTEM_LOSSES",
                    "Combined system losses exceed 20% and should be validated before investment review."));
        }
        return warnings;
    }

    private SolarSystemProfile requireSolarProfile(TechnologySystemProfile systemProfile) {
        if (systemProfile instanceof SolarSystemProfile solarProfile) {
            return solarProfile;
        }
        throw new IllegalArgumentException("Solar assessment policy requires a SolarSystemProfile.");
    }
}
