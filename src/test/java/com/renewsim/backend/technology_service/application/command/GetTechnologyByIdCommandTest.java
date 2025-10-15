package com.renewsim.backend.technology_service.application.command;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class GetTechnologyByIdCommandTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateCommandSuccessfully() {
        GetTechnologyByIdCommand command = new GetTechnologyByIdCommand(1L);
        assertEquals(1L, command.id());
    }

    @Test
    void shouldFailValidationWhenIdIsNull() {
        GetTechnologyByIdCommand command = new GetTechnologyByIdCommand(null);
        Set<ConstraintViolation<GetTechnologyByIdCommand>> violations = validator.validate(command);

        assertEquals(1, violations.size());
        assertEquals("Technology ID is required", violations.iterator().next().getMessage());
    }
}