package com.renewsim.backend.technology_service.domain.model.vo;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EnergyProduction Value Object.
 */
@Tag("unit")
class EnergyProductionTest extends BaseValueObjectTest {

    @Test
    @DisplayName("Should create valid energy production value")
    void shouldCreateValidEnergyProduction() {
        EnergyProduction production = new EnergyProduction(5000.0);
        assertDoubleEquals(5000.0, production.value());
    }

    @Test
    @DisplayName("Should reject zero energy production")
    void shouldRejectZeroProduction() {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> new EnergyProduction(0.0));
    }

    @Test
    @DisplayName("Should reject negative energy production")
    void shouldRejectNegativeProduction() {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> new EnergyProduction(-200.0));
    }

    @Test
    @DisplayName("Should compare equal productions correctly")
    void shouldCompareEqualProductions() {
        EnergyProduction p1 = new EnergyProduction(5000.0);
        EnergyProduction p2 = new EnergyProduction(5000.0);
        assertValueObjectsEqual(p1, p2);
        assertEquals(0, p1.compareTo(p2));
    }

    @Test
    @DisplayName("Should detect different productions")
    void shouldDetectDifferentProductions() {
        EnergyProduction p1 = new EnergyProduction(4000.0);
        EnergyProduction p2 = new EnergyProduction(9000.0);
        assertValueObjectsNotEqual(p1, p2);
        assertTrue(p1.compareTo(p2) < 0);
    }

    @Test
    @DisplayName("Should format toString with MWh/year")
    void shouldFormatToStringWithUnit() {
        EnergyProduction p = new EnergyProduction(5000.0);
        assertTrue(p.toString().contains("MWh/year"));
    }
}
