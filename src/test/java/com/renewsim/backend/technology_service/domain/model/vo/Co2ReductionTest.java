package com.renewsim.backend.technology_service.domain.model.vo;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Co2Reduction Value Object.
 */
@Tag("unit")
class Co2ReductionTest extends BaseValueObjectTest {

    @Test
    @DisplayName("Should create valid CO₂ reduction value")
    void shouldCreateValidCo2Reduction() {
        Co2Reduction reduction = new Co2Reduction(BigDecimal.valueOf(250));
        assertBigDecimalEquals(BigDecimal.valueOf(250), reduction.value());
    }

    @Test
    @DisplayName("Should create from double value")
    void shouldCreateFromDoubleValue() {
        Co2Reduction reduction = new Co2Reduction(150.5);
        assertBigDecimalEquals(BigDecimal.valueOf(150.5), reduction.value());
    }

    @Test
    @DisplayName("Should reject negative CO₂ reduction")
    void shouldRejectNegativeReduction() {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> new Co2Reduction(BigDecimal.valueOf(-5)));
    }

    @Test
    @DisplayName("Should compare equal CO₂ reductions correctly")
    void shouldCompareEqualReductions() {
        Co2Reduction r1 = new Co2Reduction(BigDecimal.valueOf(100));
        Co2Reduction r2 = new Co2Reduction(BigDecimal.valueOf(100));
        assertValueObjectsEqual(r1, r2);
        assertEquals(0, r1.compareTo(r2));
    }

    @Test
    @DisplayName("Should detect different CO₂ reductions")
    void shouldDetectDifferentReductions() {
        Co2Reduction r1 = new Co2Reduction(BigDecimal.valueOf(100));
        Co2Reduction r2 = new Co2Reduction(BigDecimal.valueOf(300));
        assertValueObjectsNotEqual(r1, r2);
        assertTrue(r1.compareTo(r2) < 0);
    }

    @Test
    @DisplayName("Should format toString with 'tons CO₂ saved'")
    void shouldFormatToStringWithUnit() {
        Co2Reduction c = new Co2Reduction(BigDecimal.valueOf(200));
        assertTrue(c.toString().contains("tons CO₂ saved"));
    }
}
