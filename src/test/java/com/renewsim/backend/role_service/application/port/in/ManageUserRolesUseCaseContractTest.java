package com.renewsim.backend.role_service.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;

import com.renewsim.backend.role_service.application.command.ManageUserRolesCommand;
import com.renewsim.backend.role_service.application.result.ManageUserRolesResultDTO;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract test to ensure ManageUserRolesUseCase defines the correct method
 * and can be safely invoked or mocked by application services.
 */
class ManageUserRolesUseCaseContractTest {

    @Test
    @DisplayName("should define manageRoles method with correct signature")
    void shouldDefineManageRolesMethodWithCorrectSignature() throws NoSuchMethodException {
        var method = ManageUserRolesUseCase.class.getMethod("manageRoles", ManageUserRolesCommand.class);

        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(ManageUserRolesResultDTO.class);
    }

    @Test
    @DisplayName("should allow mocking and invocation of manageRoles method")
    void shouldAllowMockingAndInvocationOfManageRoles() {
        ManageUserRolesUseCase useCase = command ->
                new ManageUserRolesResultDTO(
                        command.targetUserId(),
                        List.of("ADMIN", "MANAGER"),   
                        List.of("GUEST"),
                        true,
                        "Roles updated successfully"
                );

        ManageUserRolesCommand command = new ManageUserRolesCommand(
                1L,
                2L,
                List.of(10L, 20L),
                List.of(30L)
        );

        ManageUserRolesResultDTO result = useCase.manageRoles(command);

        assertThat(result).isNotNull();
        assertThat(result.targetUserId()).isEqualTo(2L);
        assertThat(result.assignedRoles()).containsExactly("ADMIN", "MANAGER");
        assertThat(result.revokedRoles()).containsExactly("GUEST");
        assertThat(result.success()).isTrue();
        assertThat(result.message()).isEqualTo("Roles updated successfully");
    }
}
