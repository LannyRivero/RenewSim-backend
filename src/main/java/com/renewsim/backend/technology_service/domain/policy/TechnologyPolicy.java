package com.renewsim.backend.technology_service.domain.policy;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.EnergyType;

import java.math.BigDecimal;

/**
 * Business-level policy for validating Technology compatibility rules.
 *
 * Centralizes domain-level consistency checks that involve multiple attributes,
 * ensuring technologies adhere to physical, environmental, and economic constraints.
 *
 * 🔹 Structural validations → handled by Value Objects.
 * 🔹 Cross-field business rules → handled by this policy.
 */
public final class TechnologyPolicy {

    private TechnologyPolicy() {}

    // ------------------------------------------------------------------------
    // CONSTANTS (domain thresholds)
    // ------------------------------------------------------------------------
    private static final double MAX_SOLAR_EFFICIENCY = 95.0;
    private static final double MAX_EOLIC_EFFICIENCY = 70.0;
    private static final double MAX_ENV_IMPACT_FOR_HIGH_COST = 50.0;
    private static final double HIGH_COST_THRESHOLD = 1_000_000.0;
    private static final double MAX_CO2_FOR_LOW_PRODUCTION = 100.0;
    private static final double LOW_PRODUCTION_THRESHOLD = 100.0;

    /**
     * Validates cross-field compatibility rules for a given technology.
     *
     * @param tech the technology to validate
     * @throws InvalidTechnologyParameterException if any rule is violated
     */
    public static void validateCompatibility(Technology tech) {

        double efficiency = tech.getEfficiency().value();
        BigDecimal installationCost = tech.getInstallationCost().value();
        BigDecimal maintenanceCost = tech.getMaintenanceCost().value();
        double environmentalImpact = tech.getEnvironmentalImpact().value();
        double co2Reduction = tech.getCo2Reduction().value().doubleValue();
        double production = tech.getEnergyProduction().value();

        // --------------------------------------------------------------------
        // 1️⃣ Efficiency limits per energy type
        // --------------------------------------------------------------------
        if (tech.getEnergyType() == EnergyType.SOLAR && efficiency > MAX_SOLAR_EFFICIENCY) {
            throw new InvalidTechnologyParameterException(
                "Solar technologies cannot exceed " + MAX_SOLAR_EFFICIENCY +
                "% efficiency (current: " + efficiency + ")");
        }

        if (tech.getEnergyType() == EnergyType.EOLIC && efficiency > MAX_EOLIC_EFFICIENCY) {
            throw new InvalidTechnologyParameterException(
                "Eolic technologies cannot exceed " + MAX_EOLIC_EFFICIENCY +
                "% efficiency (current: " + efficiency + ")");
        }

        // --------------------------------------------------------------------
        // 2️⃣ High-cost technologies must have low environmental impact
        // --------------------------------------------------------------------
        if (installationCost.doubleValue() > HIGH_COST_THRESHOLD && environmentalImpact > MAX_ENV_IMPACT_FOR_HIGH_COST) {
            throw new InvalidTechnologyParameterException(
                "High-cost technologies (>" + HIGH_COST_THRESHOLD +
                ") must have lower environmental impact (<= " + MAX_ENV_IMPACT_FOR_HIGH_COST + ")");
        }

        // --------------------------------------------------------------------
        // 3️⃣ Low-production technologies cannot claim excessive CO₂ reduction
        // --------------------------------------------------------------------
        if (production < LOW_PRODUCTION_THRESHOLD && co2Reduction > MAX_CO2_FOR_LOW_PRODUCTION) {
            throw new InvalidTechnologyParameterException(
                "Low-production technologies (< " + LOW_PRODUCTION_THRESHOLD +
                " MWh/year) cannot have CO₂ reduction above " + MAX_CO2_FOR_LOW_PRODUCTION + " tons/year");
        }

        // --------------------------------------------------------------------
        // 4️⃣ Maintenance cost cannot exceed installation cost
        // --------------------------------------------------------------------
        if (maintenanceCost.compareTo(installationCost) > 0) {
            throw new InvalidTechnologyParameterException(
                "Maintenance cost (" + maintenanceCost +
                ") cannot exceed installation cost (" + installationCost + ")");
        }
    }
}

