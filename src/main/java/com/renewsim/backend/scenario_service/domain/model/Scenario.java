package com.renewsim.backend.scenario_service.domain.model;

import com.renewsim.backend.scenario_service.domain.exception.InvalidScenarioParameterException;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultCapacityKw;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultConsumption;
import com.renewsim.backend.scenario_service.domain.model.vo.DefaultTariff;
import com.renewsim.backend.scenario_service.domain.model.vo.ScenarioTechnologyId;
import com.renewsim.backend.shared.domain.vo.ClimateData;
import com.renewsim.backend.shared.domain.vo.Money;

import java.util.Objects;

public final class Scenario {

    private final Long id;
    private final String name;
    private final String description;
    private final ScenarioTechnologyId technologyId;
    private final DefaultCapacityKw defaultCapacityKw;
    private final Money defaultInvestment;
    private final DefaultTariff defaultTariff;
    private final DefaultConsumption defaultConsumption;
    private final ClimateData climateProfile;
    private final boolean isActive;

    public Scenario(
            Long id,
            String name,
            String description,
            ScenarioTechnologyId technologyId,
            DefaultCapacityKw defaultCapacityKw,
            Money defaultInvestment,
            DefaultTariff defaultTariff,
            DefaultConsumption defaultConsumption,
            ClimateData climateProfile,
            boolean isActive) {
        if (name == null || name.isBlank()) {
            throw new InvalidScenarioParameterException("Scenario name cannot be null or blank");
        }

        this.id = id;
        this.name = name.trim();
        this.description = description;
        this.technologyId = Objects.requireNonNull(technologyId, "Technology id cannot be null");
        this.defaultCapacityKw = Objects.requireNonNull(defaultCapacityKw, "Default capacity cannot be null");
        this.defaultInvestment = Objects.requireNonNull(defaultInvestment, "Default investment cannot be null");
        this.defaultTariff = Objects.requireNonNull(defaultTariff, "Default tariff cannot be null");
        this.defaultConsumption = Objects.requireNonNull(defaultConsumption, "Default consumption cannot be null");
        this.climateProfile = Objects.requireNonNull(climateProfile, "Climate profile cannot be null");
        this.isActive = isActive;
    }

    public Scenario(
            String name,
            String description,
            Long technologyId,
            double defaultCapacityKw,
            Money defaultInvestment,
            double defaultTariff,
            double defaultConsumption,
            ClimateData climateProfile) {
        this(
                null,
                name,
                description,
                new ScenarioTechnologyId(technologyId),
                new DefaultCapacityKw(defaultCapacityKw),
                defaultInvestment,
                new DefaultTariff(defaultTariff),
                new DefaultConsumption(defaultConsumption),
                climateProfile,
                true);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getTechnologyId() {
        return technologyId.value();
    }

    public double getDefaultCapacityKw() {
        return defaultCapacityKw.value();
    }

    public Money getDefaultInvestment() {
        return defaultInvestment;
    }

    public double getDefaultTariff() {
        return defaultTariff.value();
    }

    public double getDefaultConsumption() {
        return defaultConsumption.value();
    }

    public ClimateData getClimateProfile() {
        return climateProfile;
    }

    public boolean isActive() {
        return isActive;
    }

    public Scenario deactivate() {
        return new Scenario(
                this.id,
                this.name,
                this.description,
                this.technologyId,
                this.defaultCapacityKw,
                this.defaultInvestment,
                this.defaultTariff,
                this.defaultConsumption,
                this.climateProfile,
                false);
    }
}
