package com.renewsim.backend.technology_service.domain.model.vo;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import com.renewsim.backend.technology_service.domain.util.ValueObjectFormatter;

import java.math.BigDecimal;

public final class MaintenanceCost implements Comparable<MaintenanceCost> {

    private final BigDecimal value;

    public MaintenanceCost(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTechnologyParameterException("Maintenance cost must be positive");
        }
        this.value = value;
    }

    public BigDecimal value() {
        return value;
    }

    @Override
    public int compareTo(MaintenanceCost other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MaintenanceCost that))
            return false;
        return this.value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return value.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return ValueObjectFormatter.format(value.doubleValue(), "€ maintenance");
    }
}
