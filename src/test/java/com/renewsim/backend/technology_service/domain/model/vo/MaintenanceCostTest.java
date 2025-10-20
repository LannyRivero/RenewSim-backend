package com.renewsim.backend.technology_service.domain.model.vo;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MaintenanceCost Value Object.
 */
@Tag("unit")
class MaintenanceCostTest extends BaseValueObjectTest {

    @Test
    @DisplayName("Should create valid maintenance cost")
    void shouldCreateValidMaintenanceCost() {
        MaintenanceCost cost = new MaintenanceCost(BigDecimal.valueOf(500));
        assertBigDecimalEquals(BigDecimal.valueOf(500), cost.value());
    }

    @Test
    @DisplayName("Should reject zero maintenance cost")
    void shouldRejectZeroCost() {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> new MaintenanceCost(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should reject negative maintenance cost")
    void shouldRejectNegativeCost() {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> new MaintenanceCost(BigDecimal.valueOf(-1)));
    }

    @Test
    @DisplayName("Should compare equal maintenance costs")
    void shouldCompareEqualMaintenanceCosts() {
        MaintenanceCost c1 = new MaintenanceCost(BigDecimal.valueOf(100));
        MaintenanceCost c2 = new MaintenanceCost(BigDecimal.valueOf(100));
        assertValueObjectsEqual(c1, c2);
    }

    @Test
    @DisplayName("Should detect different maintenance costs")
    void shouldDetectDifferentMaintenanceCosts() {
        MaintenanceCost c1 = new MaintenanceCost(BigDecimal.valueOf(100));
        MaintenanceCost c2 = new MaintenanceCost(BigDecimal.valueOf(300));
        assertValueObjectsNotEqual(c1, c2);
    }

    @Test
    @DisplayName("Should format toString with units")
    void shouldFormatToStringWithUnits() {
        MaintenanceCost c = new MaintenanceCost(BigDecimal.valueOf(500));
        assertTrue(c.toString().matches(".*\\d.*"));
    }
}
