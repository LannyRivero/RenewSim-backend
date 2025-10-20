package com.renewsim.backend.technology_service.application.command;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CreateTechnologyCommand record.
 * Verifies immutability and Jakarta Validation annotations.
 */
class CreateTechnologyCommandTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create a valid CreateTechnologyCommand with all values")
    void shouldCreateValidCommand() {
        var command = new CreateTechnologyCommand(
                "Solar Panel",
                0.85,
                12000,
                400,
                10,
                25,
                3000,
                "SOLAR");

        assertEquals("Solar Panel", command.name());
        assertEquals(0.85, command.efficiency());
        assertEquals("SOLAR", command.energyType());
    }

    @Test
    @DisplayName("Should detect missing or invalid fields via Jakarta Validation")
    void shouldValidateConstraints() {
        var invalidCommand = new CreateTechnologyCommand(
                "", // invalid name
                -1.0, // invalid efficiency
                -100, // invalid cost
                0, // invalid maintenance
                150, // invalid environmental impact
                -10, // invalid CO2
                -500, // invalid production
                "" // invalid type
        );

        Set<?> violations = validator.validate(invalidCommand);
        assertFalse(violations.isEmpty(), "Expected validation errors for invalid input");
    }
}
