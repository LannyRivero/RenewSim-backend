package com.renewsim.backend.role_service.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;

import com.renewsim.backend.role_service.application.command.RevokeRoleCommand;
import com.renewsim.backend.role_service.application.result.RoleRevocationResultDTO;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract test to ensure RevokeRoleUseCase defines the correct method
 * and can be safely mocked or invoked by application services.
 */
class RevokeRoleUseCaseContractTest {

    @Test
    @DisplayName("should define revokeRoleFromUser method with correct signature")
    void shouldDefineRevokeRoleFromUserMethodWithCorrectSignature() throws NoSuchMethodException {
        var method = RevokeRoleUseCase.class.getMethod("revokeRoleFromUser", RevokeRoleCommand.class);

        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(RoleRevocationResultDTO.class);
    }

    @Test
    @DisplayName("should allow mocking and invocation of revokeRoleFromUser method")
    void shouldAllowMockingAndInvocationOfRevokeRoleFromUser() {
        RevokeRoleUseCase useCase = command ->
                new RoleRevocationResultDTO(
                        command.targetUserId(),
                        "ADMIN",
                        true,
                        "Role revoked successfully"
                );

        RevokeRoleCommand command = new RevokeRoleCommand(1L, 2L, 3L);

        RoleRevocationResultDTO result = useCase.revokeRoleFromUser(command);

        assertThat(result).isNotNull();
        assertThat(result.targetUserId()).isEqualTo(2L);
        assertThat(result.roleRevoked()).isEqualTo("ADMIN");
        assertThat(result.success()).isTrue();
        assertThat(result.message()).isEqualTo("Role revoked successfully");
    }
}
