package com.renewsim.backend.role_service.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;

import com.renewsim.backend.role_service.application.command.CreateRoleCommand;
import com.renewsim.backend.role_service.application.result.RoleCreationResultDTO;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract test to ensure the interface CreateRoleUseCase
 * defines the expected method and behavior signature.
 */
class CreateRoleUseCaseContractTest {

    @Test
    @DisplayName("should define a createRole method that accepts CreateRoleCommand and returns RoleCreationResultDTO")
    void shouldDefineExpectedContract() throws NoSuchMethodException {
        var method = CreateRoleUseCase.class.getMethod("createRole", CreateRoleCommand.class);

        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(RoleCreationResultDTO.class);
    }

    @Test
    @DisplayName("should allow mocking and invocation of createRole")
    void shouldAllowMockingAndInvocation() {
        CreateRoleUseCase useCase = command ->
                new RoleCreationResultDTO(command.name(), "Role created successfully");

        CreateRoleCommand command = new CreateRoleCommand("ADMIN");
        RoleCreationResultDTO result = useCase.createRole(command);

        assertThat(result).isNotNull();        
        assertThat(result.roleName()).isEqualTo("ADMIN");
        assertThat(result.message()).isEqualTo("Role created successfully");
    }
}
