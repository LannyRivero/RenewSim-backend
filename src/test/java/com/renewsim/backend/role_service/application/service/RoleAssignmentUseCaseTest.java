package com.renewsim.backend.role_service.application.service;

import com.renewsim.backend.role_service.application.command.AssignRoleCommand;
import com.renewsim.backend.role_service.application.command.RevokeRoleCommand;
import com.renewsim.backend.role_service.application.port.out.UserServiceGateway;
import com.renewsim.backend.shared.observability.RoleAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoleAssignmentUseCaseTest {

    @Mock
    private UserServiceGateway userServiceGateway;
    @Mock
    private RoleValidator roleValidator;
    @Mock
    private RoleAuditService roleAuditService;

    private RoleAssignmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RoleAssignmentUseCase(userServiceGateway, roleValidator, roleAuditService);
    }

    @Test
    @DisplayName("assignRoleToUser should call user-service assign endpoint")
    void assignRoleToUser_callsAssignRole() {
        AssignRoleCommand command = new AssignRoleCommand(10L, 7L, 2L);

        var result = useCase.assignRoleToUser(command);

        verify(roleValidator).validateRoleExists(2L);
        verify(userServiceGateway).assignRole(7L, 2L);
        verify(roleAuditService).roleAssigned(10L, 7L, "ROLE_2");

        assertThat(result.roleAssigned()).isEqualTo("ROLE_2");
    }

    @Test
    @DisplayName("revokeRoleFromUser should call user-service remove endpoint")
    void revokeRoleFromUser_callsRemoveRole() {
        RevokeRoleCommand command = new RevokeRoleCommand(10L, 7L, 2L);

        var result = useCase.revokeRoleFromUser(command);

        verify(roleValidator).validateRoleExists(2L);
        verify(userServiceGateway).removeRole(7L, 2L);
        verify(roleAuditService).roleRevoked(10L, 7L, "ROLE_2");

        assertThat(result.roleRevoked()).isEqualTo("ROLE_2");
    }
}
