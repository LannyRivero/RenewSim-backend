package com.renewsim.backend.technology_service.domain.factory;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import com.renewsim.backend.technology_service.domain.model.Technology;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TechnologyFactory.
 * Verifies correct creation, validation, and policy enforcement of Technology
 * aggregates.
 */
class TechnologyFactoryTest {

        // --------------------------------------------------------------
        // ✅ CREATION TESTS
        // --------------------------------------------------------------

        @Test
        @DisplayName("Should create valid Technology successfully")
        void shouldCreateValidTechnology() {
                Technology tech = TechnologyFactory.create(
                                "Solar Panel",
                                85.0,
                                12000.0,
                                400.0,
                                5.0,
                                25.0,
                                18.0,
                                "SOLAR");

                assertNotNull(tech);
                assertEquals("Solar Panel", tech.getName());
                assertEquals(85.0, tech.getEfficiency().value());
                assertEquals("SOLAR", tech.getEnergyType().name());
        }

        // --------------------------------------------------------------
        // ❌ INVALID VALUE TESTS (handled by Value Objects)
        // --------------------------------------------------------------

        @Test
        @DisplayName("Should reject invalid efficiency values (<0 or >100)")
        void shouldRejectInvalidEfficiency() {
        assertThrows(InvalidTechnologyParameterException.class, () -> TechnologyFactory.create(
                        "Bad Tech",
                        120.0, 
                        10000.0,
                        500.0,
                        5.0,
                        25.0,
                        18.0,
                        "SOLAR"));
        }

        @Test
        @DisplayName("Should reject negative installation cost")
        void shouldRejectNegativeInstallationCost() {
        assertThrows(InvalidTechnologyParameterException.class, () -> TechnologyFactory.create(
                        "Faulty System",
                        70.0,
                        -5000.0, 
                        200.0,
                        10.0,
                        15.0,
                        35.0,
                        "EOLIC"));
        }

        @Test
        @DisplayName("Should reject blank technology name")
        void shouldRejectBlankName() {
                assertThrows(InvalidTechnologyParameterException.class, () -> TechnologyFactory.create(
                                "   ",
                                75.0,
                                8000.0,
                                300.0,
                                10.0,
                                25.0,
                                65.0,
                                "HYDRO"));
        }

        // --------------------------------------------------------------
        // ❌ BUSINESS-LEVEL TESTS (handled by TechnologyPolicy)
        // --------------------------------------------------------------

        @Test
        @DisplayName("Should reject solar technology exceeding 95% efficiency (policy rule)")
        void shouldRejectSolarWithUnrealisticEfficiency() {
                assertThrows(InvalidTechnologyParameterException.class, () -> TechnologyFactory.create(
                                "Solar Ultra",
                                99.0, 
                                12000.0,
                                400.0,
                                10.0,
                                25.0,
                                18.0,
                                "SOLAR"));
        }

        @Test
        @DisplayName("Should reject technology with maintenance cost higher than installation cost (policy rule)")
        void shouldRejectMaintenanceHigherThanInstallation() {
                assertThrows(InvalidTechnologyParameterException.class, () -> TechnologyFactory.create(
                                "GeoTech",
                                80.0,
                                4000.0, 
                                6000.0, 
                                15.0,
                                25.0,
                                35.0,
                                "GEOTHERMAL"));
        }

        // --------------------------------------------------------------
        // ✅ VALID EDGE CASES
        // --------------------------------------------------------------

        @Test
        @DisplayName("Should create high-cost low-impact technology successfully")
        void shouldCreateHighCostLowImpactTechnology() {
                Technology tech = TechnologyFactory.create(
                                "Hydro Plant",
                                88.0,
                                1_200_000.0, 
                                6000.0,
                                30.0, 
                                100.0,
                                65.0,
                                "HYDRO");

                assertNotNull(tech);
                assertEquals("Hydro Plant", tech.getName());
        }

        @Test
        @DisplayName("Should handle minimum edge values gracefully")
        void shouldHandleEdgeValues() {
                Technology tech = TechnologyFactory.create(
                                "Minimal Tech",
                                0.0,
                                1.0,
                                1.0,
                                0.0,
                                0.0,
                                1.0,
                                "SOLAR");

                assertEquals(0.0, tech.getEfficiency().value());
                assertEquals(1.0, tech.getInstallationCost().value().doubleValue());
                assertEquals(1.0, tech.getMaintenanceCost().value().doubleValue());
        }
}
