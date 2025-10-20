package com.renewsim.backend.technology_service.domain.model.vo;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import com.renewsim.backend.technology_service.domain.util.ValueObjectFormatter;

import java.math.BigDecimal;

/**
 * Value Object representing CO₂ reduction in tons per year.
 * Ensures immutability, validation, and precise decimal handling.
 */
public final class Co2Reduction implements Comparable<Co2Reduction> {

    private final BigDecimal value;

    /**
     * Primary constructor using BigDecimal (recommended for precision).
     */
    public Co2Reduction(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTechnologyParameterException("CO₂ reduction cannot be negative or null");
        }
        this.value = value;
    }

    /**
     * Convenience constructor accepting double, delegates to BigDecimal-based constructor.
     * Useful for factories or test data creation.
     */
    public Co2Reduction(double value) {
        this(BigDecimal.valueOf(value));
    }

    /**
     * Returns the CO₂ reduction value (in tons per year).
     */
    public BigDecimal value() {
        return value;
    }

    @Override
    public int compareTo(Co2Reduction other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Co2Reduction that)) return false;
        return this.value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return value.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return ValueObjectFormatter.format(value.doubleValue(), "tons CO₂ saved");
    }
}
