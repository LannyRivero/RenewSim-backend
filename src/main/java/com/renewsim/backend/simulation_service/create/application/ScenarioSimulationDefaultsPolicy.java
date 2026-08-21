package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationCurrencyException;
import com.renewsim.backend.simulation_service.domain.model.vo.Currency;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectLifetime;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationEconomics;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationSystem;
import org.springframework.stereotype.Component;

@Component
public class ScenarioSimulationDefaultsPolicy {

    private static final double DEFAULT_PERFORMANCE_RATIO = 0.81;
    private static final double DEFAULT_DEGRADATION_RATE_ANNUAL_PCT = 0.5;
    private static final double DEFAULT_AVAILABILITY_PCT = 99.0;
    private static final double DEFAULT_LOSSES_INVERTER = 2.0;
    private static final double DEFAULT_LOSSES_TEMPERATURE = 6.0;
    private static final double DEFAULT_LOSSES_WIRING = 1.0;
    private static final double DEFAULT_LOSSES_SOILING = 3.0;
    private static final double DEFAULT_LOSSES_OTHER = 1.0;
    private static final int DEFAULT_PROJECT_LIFETIME_YEARS = 20;
    private static final double DEFAULT_OPEX_ANNUAL = 0.0;
    private static final double DEFAULT_EXPORT_PRICE_PER_KWH = 0.07;
    private static final double DEFAULT_DISCOUNT_RATE_PCT = 8.0;
    private static final String SUPPORTED_SIMULATION_CURRENCY = "EUR";

    public SimulationSystem buildSystem(double defaultCapacityKw) {
        return new SimulationSystem(
                defaultCapacityKw,
                DEFAULT_PERFORMANCE_RATIO,
                DEFAULT_DEGRADATION_RATE_ANNUAL_PCT,
                DEFAULT_AVAILABILITY_PCT,
                new SimulationSystem.LossesPct(
                        DEFAULT_LOSSES_INVERTER,
                        DEFAULT_LOSSES_TEMPERATURE,
                        DEFAULT_LOSSES_WIRING,
                        DEFAULT_LOSSES_SOILING,
                        DEFAULT_LOSSES_OTHER));
    }

    public SimulationEconomics buildEconomics(double investmentAmount, String scenarioCurrency, double defaultTariff) {
        return new SimulationEconomics(
                Currency.of(resolveSimulationCurrency(scenarioCurrency)),
                investmentAmount,
                DEFAULT_OPEX_ANNUAL,
                defaultTariff,
                DEFAULT_EXPORT_PRICE_PER_KWH,
                DEFAULT_DISCOUNT_RATE_PCT,
                ProjectLifetime.of(DEFAULT_PROJECT_LIFETIME_YEARS));
    }

    String resolveSimulationCurrency(String scenarioCurrency) {
        if (!SUPPORTED_SIMULATION_CURRENCY.equalsIgnoreCase(scenarioCurrency == null ? null : scenarioCurrency.trim())) {
            throw new InvalidSimulationCurrencyException(
                    "VALIDATION_ERROR: scenario defaultInvestmentCurrency must be " + SUPPORTED_SIMULATION_CURRENCY);
        }
        return SUPPORTED_SIMULATION_CURRENCY;
    }
}
