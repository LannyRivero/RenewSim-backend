package com.renewsim.backend.technology_service.domain.model.vo;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import com.renewsim.backend.technology_service.domain.util.ValueObjectFormatter;

public final class Efficiency implements Comparable<Efficiency> {

    private final double value;

    public Efficiency(double value) {
        if (value < 0 || value > 100) {
            throw new InvalidTechnologyParameterException("Efficiency must be between 0 and 100");
        }
        this.value = value;
    }

    public double value() {
        return value;
    }

    @Override
    public int compareTo(Efficiency other) {
        return Double.compare(this.value, other.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Efficiency that))
            return false;
        return Double.compare(this.value, that.value) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(value);
    }

    @Override
    public String toString() {
        return ValueObjectFormatter.format(value, "%");
    }
}
