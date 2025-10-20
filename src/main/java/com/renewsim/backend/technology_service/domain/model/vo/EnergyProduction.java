package com.renewsim.backend.technology_service.domain.model.vo;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import com.renewsim.backend.technology_service.domain.util.ValueObjectFormatter;

/**
 * Value Object representing annual energy production (in MWh/year).
 */
public final class EnergyProduction implements Comparable<EnergyProduction> {

    private final double value;

    public EnergyProduction(double value) {
        if (value <= 0) {
            throw new InvalidTechnologyParameterException("Energy production must be greater than zero");
        }
        this.value = value;
    }

    public double value() {
        return value;
    }

    public boolean isHighProduction() {
        return value > 5000.0;
    }

    public EnergyProduction add(EnergyProduction other) {
        return new EnergyProduction(this.value + other.value);
    }

    @Override
    public int compareTo(EnergyProduction other) {
        return Double.compare(this.value, other.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof EnergyProduction that))
            return false;
        return Double.compare(this.value, that.value) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    @Override
    public String toString() {
        return ValueObjectFormatter.format(value, "MWh/year");
    }
}
