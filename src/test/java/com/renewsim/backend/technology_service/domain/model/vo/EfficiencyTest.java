package com.renewsim.backend.technology_service.domain.model.vo;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Efficiency Value Object.
 * Ensures domain invariants and comparison logic are respected.
 */
@Tag("unit")
class EfficiencyTest extends BaseValueObjectTest {

    @Test
    @DisplayName("Should create valid efficiency between 0 and 100")
    void shouldCreateValidEfficiency() {
        Efficiency e = new Efficiency(85.0);
        assertDoubleEquals(85.0, e.value());
    }

    @Test
    @DisplayName("Should reject efficiency below 0")
    void shouldRejectNegativeEfficiency() {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> new Efficiency(-5.0));
    }

    @Test
    @DisplayName("Should reject efficiency above 100")
    void shouldRejectOver100Efficiency() {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> new Efficiency(120.0));
    }

    @Test
    @DisplayName("Should compare equal efficiencies correctly")
    void shouldCompareEqualEfficiencies() {
        Efficiency e1 = new Efficiency(75.0);
        Efficiency e2 = new Efficiency(75.0);
        assertValueObjectsEqual(e1, e2);
        assertEquals(0, e1.compareTo(e2));
    }

    @Test
    @DisplayName("Should detect different efficiencies as not equal")
    void shouldDetectDifferentEfficiencies() {
        Efficiency e1 = new Efficiency(70.0);
        Efficiency e2 = new Efficiency(90.0);
        assertValueObjectsNotEqual(e1, e2);
        assertTrue(e1.compareTo(e2) < 0);
    }

    @Test
    @DisplayName("Should format toString with % symbol")
    void shouldFormatToStringWithPercent() {
        Efficiency e = new Efficiency(90.0);
        assertTrue(e.toString().contains("%"));
    }
}
