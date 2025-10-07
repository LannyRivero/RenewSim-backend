package com.renewsim.backend.role_service.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;

import com.renewsim.backend.role_service.application.command.AssignRoleCommand;
import com.renewsim.backend.role_service.application.result.RoleAssignmentResultDTO;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract test to ensure that the AssignRoleUseCase interface
 * defines the expected method and can be invoked with valid parameters.
 */
class AssignRoleUseCaseContractTest {

    @Test
    @DisplayName("should define assignRoleToUser method with correct signature")
    void shouldDefineAssignRoleToUserMethodWithCorrectSignature() throws NoSuchMethodException {
        var method = AssignRoleUseCase.class.getMethod("assignRoleToUser", AssignRoleCommand.class);

        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(RoleAssignmentResultDTO.class);
    }

    @Test
    @DisplayName("should allow mocking and invocation of assignRoleToUser method")
    void shouldAllowMockingAndInvocationOfAssignRoleToUser() {
        AssignRoleUseCase useCase = command ->
                new RoleAssignmentResultDTO(
                        command.targetUserId(),
                        "ADMIN",
                        true,
                        "Role assigned successfully"
                );

        AssignRoleCommand command = new AssignRoleCommand(1L, 2L, 3L);

        RoleAssignmentResultDTO result = useCase.assignRoleToUser(command);

        assertThat(result).isNotNull();
        assertThat(result.targetUserId()).isEqualTo(2L);
        assertThat(result.roleAssigned()).isEqualTo("ADMIN");
        assertThat(result.success()).isTrue();
        assertThat(result.message()).isEqualTo("Role assigned successfully");
    }
}
