package com.renewsim.backend.technology_service.application.command;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class DeleteTechnologyCommandTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldValidateWhenIdIsNotNull() {
        DeleteTechnologyCommand command = new DeleteTechnologyCommand(1L);
        Set<ConstraintViolation<DeleteTechnologyCommand>> violations = validator.validate(command);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldNotValidateWhenIdIsNull() {
        DeleteTechnologyCommand command = new DeleteTechnologyCommand(null);
        Set<ConstraintViolation<DeleteTechnologyCommand>> violations = validator.validate(command);
        assertFalse(violations.isEmpty());
        assertEquals("Technology ID is required", violations.iterator().next().getMessage());
    }
}