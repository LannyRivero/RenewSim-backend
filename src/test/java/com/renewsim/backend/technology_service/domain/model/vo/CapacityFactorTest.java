package com.renewsim.backend.technology_service.domain.model.vo;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class CapacityFactorTest extends BaseValueObjectTest {

    @Test
    @DisplayName("Should create valid capacity factor value")
    void shouldCreateValidCapacityFactor() {
        CapacityFactor cf = new CapacityFactor(35.0);
        assertDoubleEquals(35.0, cf.value());
    }

    @Test
    @DisplayName("Should reject zero capacity factor")
    void shouldRejectZero() {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> new CapacityFactor(0.0));
    }

    @Test
    @DisplayName("Should reject negative capacity factor")
    void shouldRejectNegative() {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> new CapacityFactor(-10.0));
    }

    @Test
    @DisplayName("Should reject capacity factor over 100")
    void shouldRejectOver100() {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> new CapacityFactor(150.0));
    }

    @Test
    @DisplayName("Should accept boundary value 100")
    void shouldAccept100() {
        CapacityFactor cf = new CapacityFactor(100.0);
        assertDoubleEquals(100.0, cf.value());
    }

    @Test
    @DisplayName("Should compare equal factors correctly")
    void shouldCompareEqualFactors() {
        CapacityFactor cf1 = new CapacityFactor(35.0);
        CapacityFactor cf2 = new CapacityFactor(35.0);
        assertValueObjectsEqual(cf1, cf2);
        assertEquals(0, cf1.compareTo(cf2));
    }

    @Test
    @DisplayName("Should detect different factors")
    void shouldDetectDifferentFactors() {
        CapacityFactor cf1 = new CapacityFactor(18.0);
        CapacityFactor cf2 = new CapacityFactor(35.0);
        assertValueObjectsNotEqual(cf1, cf2);
        assertTrue(cf1.compareTo(cf2) < 0);
    }

    @Test
    @DisplayName("Should format toString with percentage")
    void shouldFormatToStringWithUnit() {
        CapacityFactor cf = new CapacityFactor(35.0);
        assertTrue(cf.toString().contains("%"));
    }
}
