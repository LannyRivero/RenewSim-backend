package com.renewsim.backend.technology_service.domain.model.vo;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EnvironmentalImpact Value Object.
 */
@Tag("unit")
class EnvironmentalImpactTest extends BaseValueObjectTest {

    @Test
    @DisplayName("Should create valid environmental impact (0–100)")
    void shouldCreateValidImpact() {
        EnvironmentalImpact impact = new EnvironmentalImpact(25.5);
        assertDoubleEquals(25.5, impact.value());
    }

    @Test
    @DisplayName("Should reject negative impact")
    void shouldRejectNegativeImpact() {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> new EnvironmentalImpact(-10.0));
    }

    @Test
    @DisplayName("Should reject impact above 100")
    void shouldRejectOver100Impact() {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> new EnvironmentalImpact(120.0));
    }

    @Test
    @DisplayName("Should compare equal impacts correctly")
    void shouldCompareEqualImpacts() {
        EnvironmentalImpact i1 = new EnvironmentalImpact(40.0);
        EnvironmentalImpact i2 = new EnvironmentalImpact(40.0);
        assertValueObjectsEqual(i1, i2);
        assertEquals(0, i1.compareTo(i2));
    }

    @Test
    @DisplayName("Should detect different impacts")
    void shouldDetectDifferentImpacts() {
        EnvironmentalImpact i1 = new EnvironmentalImpact(20.0);
        EnvironmentalImpact i2 = new EnvironmentalImpact(60.0);
        assertValueObjectsNotEqual(i1, i2);
    }
    
}
