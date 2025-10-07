package com.renewsim.backend.role_service.application.service.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.renewsim.backend.role_service.application.command.CreateRoleCommand;

class CreateRoleCommandTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("should validate successfully when role name is valid")
    void shouldValidateSuccessfully_whenRoleNameIsValid() {
        CreateRoleCommand command = new CreateRoleCommand("ADMIN");

        Set<ConstraintViolation<CreateRoleCommand>> violations = validator.validate(command);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("should fail when role name is null")
    void shouldFail_whenRoleNameIsNull() {
        CreateRoleCommand command = new CreateRoleCommand(null);

        Set<ConstraintViolation<CreateRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateRoleCommand> violation = violations.iterator().next();

        assertThat(violation.getMessage()).isEqualTo("Role name cannot be blank");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    @DisplayName("should fail when role name is blank")
    void shouldFail_whenRoleNameIsBlank() {
        CreateRoleCommand command = new CreateRoleCommand("   ");

        Set<ConstraintViolation<CreateRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateRoleCommand> violation = violations.iterator().next();

        assertThat(violation.getMessage()).isEqualTo("Role name cannot be blank");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    @DisplayName("should fail when role name is too short")
    void shouldFail_whenRoleNameIsTooShort() {
        CreateRoleCommand command = new CreateRoleCommand("AB");

        Set<ConstraintViolation<CreateRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateRoleCommand> violation = violations.iterator().next();

        assertThat(violation.getMessage()).isEqualTo("Role name must be between 3 and 50 characters");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    @DisplayName("should fail when role name is too long")
    void shouldFail_whenRoleNameIsTooLong() {
        String longName = "A".repeat(51);
        CreateRoleCommand command = new CreateRoleCommand(longName);

        Set<ConstraintViolation<CreateRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        ConstraintViolation<CreateRoleCommand> violation = violations.iterator().next();

        assertThat(violation.getMessage()).isEqualTo("Role name must be between 3 and 50 characters");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    @DisplayName("should generate expected values from record")
    void shouldGenerateExpectedValuesFromRecord() {
        CreateRoleCommand command = new CreateRoleCommand("MANAGER");

        assertThat(command.name()).isEqualTo("MANAGER");
    }
}
