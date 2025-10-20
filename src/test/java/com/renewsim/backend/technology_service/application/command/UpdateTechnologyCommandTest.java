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
 * Unit tests for UpdateTechnologyCommand record.
 */
class UpdateTechnologyCommandTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create a valid UpdateTechnologyCommand")
    void shouldCreateValidCommand() {
        var command = new UpdateTechnologyCommand(
                1L,
                "Wind Turbine",
                0.7,
                25000,
                1000,
                20,
                50,
                5000,
                "EOLIC");

        assertEquals(1L, command.id());
        assertEquals("Wind Turbine", command.name());
        assertEquals("EOLIC", command.energyType());
    }

    @Test
    @DisplayName("Should detect invalid UpdateTechnologyCommand values")
    void shouldValidateInvalidCommand() {
        var invalid = new UpdateTechnologyCommand(
                null, "", -0.5, -10000, -200, 200, -50, -1000, "");

        Set<?> violations = validator.validate(invalid);
        assertFalse(violations.isEmpty(), "Expected validation errors");
    }
}
