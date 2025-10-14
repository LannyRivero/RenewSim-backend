package com.renewsim.backend.technology_service.domain.model.vo;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base test utilities for Value Object verification.
 * Provides reusable assertions for numerical and equality validations.
 */
public abstract class BaseValueObjectTest {

    /**
     * Compares two BigDecimals ignoring scale differences.
     * Example: 10000.0 == 10000.00
     */
    protected static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertNotNull(expected, "Expected value cannot be null");
        assertNotNull(actual, "Actual value cannot be null");

        boolean equal = expected.compareTo(actual) == 0;
        assertTrue(equal,
                () -> "Expected BigDecimal <" + expected + "> but was <" + actual + ">");
    }

    /**
     * Compares two doubles with precision tolerance (1e-6).
     * Prevents false negatives due to floating-point rounding.
     */
    protected static void assertDoubleEquals(double expected, double actual) {
        assertEquals(expected, actual, 1e-6,
                "Expected <" + expected + "> but was <" + actual + ">");
    }

    /**
     * Asserts two Value Objects are equal and have the same hashCode.
     */
    protected static <T> void assertValueObjectsEqual(T vo1, T vo2) {
        assertEquals(vo1, vo2, "Value Objects should be equal");
        assertEquals(vo1.hashCode(), vo2.hashCode(), "Value Objects hashCode must be equal");
    }

    /**
     * Asserts two Value Objects are different and have distinct hashCode.
     */
    protected static <T> void assertValueObjectsNotEqual(T vo1, T vo2) {
        assertNotEquals(vo1, vo2, "Value Objects should not be equal");
        assertNotEquals(vo1.hashCode(), vo2.hashCode(), "Value Objects hashCode should differ");
    }
}
