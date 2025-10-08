package com.renewsim.backend.role_service.application.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class ManageUserRolesCommandTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("should validate successfully when all fields are valid")
    void shouldValidateSuccessfully_whenAllFieldsAreValid() {
        ManageUserRolesCommand command = new ManageUserRolesCommand(
                1L,
                2L,
                List.of(3L, 4L),
                List.of(5L));

        Set<ConstraintViolation<ManageUserRolesCommand>> violations = validator.validate(command);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("should fail when requesterId is null")
    void shouldFail_whenRequesterIdIsNull() {
        ManageUserRolesCommand command = new ManageUserRolesCommand(
                null,
                2L,
                List.of(3L),
                List.of(4L));

        Set<ConstraintViolation<ManageUserRolesCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        ConstraintViolation<ManageUserRolesCommand> violation = violations.iterator().next();

        assertThat(violation.getMessage()).isEqualTo("Requester ID cannot be null");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("requesterId");
    }

    @Test
    @DisplayName("should fail when requesterId is not positive")
    void shouldFail_whenRequesterIdIsNotPositive() {
        ManageUserRolesCommand command = new ManageUserRolesCommand(
                0L,
                2L,
                List.of(3L),
                List.of(4L));

        Set<ConstraintViolation<ManageUserRolesCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Requester ID must be positive");
    }

    @Test
    @DisplayName("should fail when targetUserId is null")
    void shouldFail_whenTargetUserIdIsNull() {
        ManageUserRolesCommand command = new ManageUserRolesCommand(
                1L,
                null,
                List.of(3L),
                List.of(4L));

        Set<ConstraintViolation<ManageUserRolesCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Target User ID cannot be null");
    }

    @Test
    @DisplayName("should fail when targetUserId is not positive")
    void shouldFail_whenTargetUserIdIsNotPositive() {
        ManageUserRolesCommand command = new ManageUserRolesCommand(
                1L,
                -5L,
                List.of(3L),
                List.of(4L));

        Set<ConstraintViolation<ManageUserRolesCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Target User ID must be positive");
    }

    @Test
    @DisplayName("should fail when rolesToAssign list is empty")
    void shouldFail_whenRolesToAssignIsEmpty() {
        ManageUserRolesCommand command = new ManageUserRolesCommand(
                1L,
                2L,
                List.of(),
                List.of(4L));

        Set<ConstraintViolation<ManageUserRolesCommand>> violations = validator.validate(command);

        assertThat(violations).hasSize(1);
        ConstraintViolation<ManageUserRolesCommand> violation = violations.iterator().next();

        assertThat(violation.getMessage()).isEqualTo("At least one role to assign is required");
        assertThat(violation.getPropertyPath().toString()).isEqualTo("rolesToAssign");
    }

    @Test
    @DisplayName("should fail when rolesToAssign contains null elements")
    void shouldFail_whenRolesToAssignContainsNull() {
        ManageUserRolesCommand command = new ManageUserRolesCommand(
                1L,
                2L,
                List.of(1L, null, 3L),
                List.of(4L));

        Set<ConstraintViolation<ManageUserRolesCommand>> violations = validator.validate(command);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("must not be null");
    }

    @Test
    @DisplayName("should fail when rolesToAssign contains negative elements")
    void shouldFail_whenRolesToAssignContainsNegative() {
        ManageUserRolesCommand command = new ManageUserRolesCommand(
                1L,
                2L,
                List.of(1L, -10L, 3L),
                List.of(4L));

        Set<ConstraintViolation<ManageUserRolesCommand>> violations = validator.validate(command);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("must be positive");
    }

    @Test
    @DisplayName("should fail when rolesToRevoke contains null or negative elements")
    void shouldFail_whenRolesToRevokeContainsInvalidValues() {
        ManageUserRolesCommand command = new ManageUserRolesCommand(
                1L,
                2L,
                List.of(3L),
                List.of(null, -2L));

        Set<ConstraintViolation<ManageUserRolesCommand>> violations = validator.validate(command);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("must not be null", "must be positive");
    }

    @Test
    @DisplayName("should generate expected values from record")
    void shouldGenerateExpectedValuesFromRecord() {
        ManageUserRolesCommand command = new ManageUserRolesCommand(
                1L,
                2L,
                List.of(10L, 20L),
                List.of(30L));

        assertThat(command.requesterId()).isEqualTo(1L);
        assertThat(command.targetUserId()).isEqualTo(2L);
        assertThat(command.rolesToAssign()).containsExactly(10L, 20L);
        assertThat(command.rolesToRevoke()).containsExactly(30L);
    }
}
