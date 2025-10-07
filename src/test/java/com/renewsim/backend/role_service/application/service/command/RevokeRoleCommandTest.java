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

import com.renewsim.backend.role_service.application.command.RevokeRoleCommand;

class RevokeRoleCommandTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("should validate successfully when all fields are valid")
    void shouldValidateSuccessfully_whenAllFieldsAreValid() {
        RevokeRoleCommand command = new RevokeRoleCommand(1L, 2L, 3L);

        Set<ConstraintViolation<RevokeRoleCommand>> violations = validator.validate(command);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("should fail when requesterId is null")
    void shouldFail_whenRequesterIdIsNull() {
        RevokeRoleCommand command = new RevokeRoleCommand(null, 2L, 3L);

        Set<ConstraintViolation<RevokeRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        ConstraintViolation<RevokeRoleCommand> violation = violations.iterator().next();

        assertThat(violation.getMessage()).isEqualTo("Requester ID cannot be null");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("requesterId");
    }

    @Test
    @DisplayName("should fail when requesterId is not positive")
    void shouldFail_whenRequesterIdIsNotPositive() {
        RevokeRoleCommand command = new RevokeRoleCommand(0L, 2L, 3L);

        Set<ConstraintViolation<RevokeRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        ConstraintViolation<RevokeRoleCommand> violation = violations.iterator().next();

        assertThat(violation.getMessage()).isEqualTo("Requester ID must be positive");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("requesterId");
    }

    @Test
    @DisplayName("should fail when targetUserId is null")
    void shouldFail_whenTargetUserIdIsNull() {
        RevokeRoleCommand command = new RevokeRoleCommand(1L, null, 3L);

        Set<ConstraintViolation<RevokeRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        ConstraintViolation<RevokeRoleCommand> violation = violations.iterator().next();

        assertThat(violation.getMessage()).isEqualTo("Target User ID cannot be null");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("targetUserId");
    }

    @Test
    @DisplayName("should fail when targetUserId is not positive")
    void shouldFail_whenTargetUserIdIsNotPositive() {
        RevokeRoleCommand command = new RevokeRoleCommand(1L, -5L, 3L);

        Set<ConstraintViolation<RevokeRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        ConstraintViolation<RevokeRoleCommand> violation = violations.iterator().next();

        assertThat(violation.getMessage()).isEqualTo("Target User ID must be positive");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("targetUserId");
    }

    @Test
    @DisplayName("should fail when roleId is null")
    void shouldFail_whenRoleIdIsNull() {
        RevokeRoleCommand command = new RevokeRoleCommand(1L, 2L, null);

        Set<ConstraintViolation<RevokeRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        ConstraintViolation<RevokeRoleCommand> violation = violations.iterator().next();

        assertThat(violation.getMessage()).isEqualTo("Role ID cannot be null");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("roleId");
    }

    @Test
    @DisplayName("should fail when roleId is not positive")
    void shouldFail_whenRoleIdIsNotPositive() {
        RevokeRoleCommand command = new RevokeRoleCommand(1L, 2L, -10L);

        Set<ConstraintViolation<RevokeRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        ConstraintViolation<RevokeRoleCommand> violation = violations.iterator().next();

        assertThat(violation.getMessage()).isEqualTo("Role ID must be positive");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("roleId");
    }

    @Test
    @DisplayName("should expose expected values from record getters")
    void shouldExposeExpectedValuesFromRecordGetters() {
        RevokeRoleCommand command = new RevokeRoleCommand(10L, 20L, 30L);

        assertThat(command.requesterId()).isEqualTo(10L);
        assertThat(command.targetUserId()).isEqualTo(20L);
        assertThat(command.roleId()).isEqualTo(30L);
    }
}
