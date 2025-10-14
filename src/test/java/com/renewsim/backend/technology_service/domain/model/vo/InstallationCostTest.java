package com.renewsim.backend.technology_service.domain.model.vo;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InstallationCost Value Object.
 */
@Tag("unit")
class InstallationCostTest extends BaseValueObjectTest {

    @Test
    @DisplayName("Should create valid installation cost")
    void shouldCreateValidInstallationCost() {
        InstallationCost cost = new InstallationCost(BigDecimal.valueOf(10000.00));
        assertBigDecimalEquals(BigDecimal.valueOf(10000.00), cost.value());
    }

    @Test
    @DisplayName("Should reject zero installation cost")
    void shouldRejectZeroCost() {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> new InstallationCost(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should reject negative installation cost")
    void shouldRejectNegativeCost() {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> new InstallationCost(BigDecimal.valueOf(-5000)));
    }

    @Test
    @DisplayName("Should compare equal installation costs correctly")
    void shouldCompareEqualInstallationCosts() {
        InstallationCost c1 = new InstallationCost(BigDecimal.valueOf(8000));
        InstallationCost c2 = new InstallationCost(BigDecimal.valueOf(8000));
        assertValueObjectsEqual(c1, c2);
    }

    @Test
    @DisplayName("Should detect different installation costs")
    void shouldDetectDifferentInstallationCosts() {
        InstallationCost c1 = new InstallationCost(BigDecimal.valueOf(5000));
        InstallationCost c2 = new InstallationCost(BigDecimal.valueOf(9000));
        assertValueObjectsNotEqual(c1, c2);
    }

    @Test
    @DisplayName("Should format toString with currency symbol or units")
    void shouldFormatToStringWithCurrency() {
        InstallationCost c = new InstallationCost(BigDecimal.valueOf(10000));
        assertTrue(c.toString().matches(".*\\d.*"));
    }
}
