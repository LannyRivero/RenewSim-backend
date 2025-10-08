package com.renewsim.backend.role_service.application.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class AssignRoleCommandTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("should validate successfully when all fields are valid")
    void shouldValidateSuccessfully_whenFieldsAreValid() {
        AssignRoleCommand command = new AssignRoleCommand(1L, 2L, 3L);

        Set<ConstraintViolation<AssignRoleCommand>> violations = validator.validate(command);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("should fail when requesterId is null")
    void shouldFail_whenRequesterIdIsNull() {
        AssignRoleCommand command = new AssignRoleCommand(null, 2L, 3L);

        Set<ConstraintViolation<AssignRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Requester ID cannot be null");
    }

    @Test
    @DisplayName("should fail when targetUserId is null")
    void shouldFail_whenTargetUserIdIsNull() {
        AssignRoleCommand command = new AssignRoleCommand(1L, null, 3L);

        Set<ConstraintViolation<AssignRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Target User ID cannot be null");
    }

    @Test
    @DisplayName("should fail when roleId is null")
    void shouldFail_whenRoleIdIsNull() {
        AssignRoleCommand command = new AssignRoleCommand(1L, 2L, null);

        Set<ConstraintViolation<AssignRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Role ID cannot be null");
    }

    @Test
    @DisplayName("should fail when requesterId is not positive")
    void shouldFail_whenRequesterIdIsNotPositive() {
        AssignRoleCommand command = new AssignRoleCommand(0L, 2L, 3L);

        Set<ConstraintViolation<AssignRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Requester ID must be positive");
    }

    @Test
    @DisplayName("should fail when targetUserId is not positive")
    void shouldFail_whenTargetUserIdIsNotPositive() {
        AssignRoleCommand command = new AssignRoleCommand(1L, -5L, 3L);

        Set<ConstraintViolation<AssignRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Target User ID must be positive");
    }

    @Test
    @DisplayName("should fail when roleId is not positive")
    void shouldFail_whenRoleIdIsNotPositive() {
        AssignRoleCommand command = new AssignRoleCommand(1L, 2L, -1L);

        Set<ConstraintViolation<AssignRoleCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Role ID must be positive");
    }

    @Test
    @DisplayName("should generate expected values from record")
    void shouldGenerateExpectedValuesFromRecord() {
        AssignRoleCommand command = new AssignRoleCommand(10L, 20L, 30L);

        assertThat(command.requesterId()).isEqualTo(10L);
        assertThat(command.targetUserId()).isEqualTo(20L);
        assertThat(command.roleId()).isEqualTo(30L);
    }
}
