package com.renewsim.backend.role_service.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;

import com.renewsim.backend.user_service.dto.UpdateUserRolesRequestDTO;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract test to ensure UserServiceGateway defines the correct method signature
 * and can be mocked or invoked safely from the Role Service.
 */
class UserServiceGatewayContractTest {

    @Test
    @DisplayName("should define updateUserRoles method with correct signature")
    void shouldDefineUpdateUserRolesMethodWithCorrectSignature() throws NoSuchMethodException {
        var method = UserServiceGateway.class.getMethod("updateUserRoles", Long.class, UpdateUserRolesRequestDTO.class);

        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(void.class);
    }

    @Test
    @DisplayName("should allow mocking and invocation of updateUserRoles with single roles list")
    void shouldAllowMockingAndInvocationOfUpdateUserRoles() {
        // Arrange — mock implementation using the real DTO structure
        UserServiceGateway gateway = (userId, request) -> {
            assertThat(userId).isPositive();
            assertThat(request).isNotNull();
            assertThat(request.roles()).isNotEmpty();
            System.out.printf("Updating user %d with roles: %s%n", userId, request.roles());
        };

        // Act
        UpdateUserRolesRequestDTO dto = new UpdateUserRolesRequestDTO(List.of("ADMIN", "USER"));

        gateway.updateUserRoles(42L, dto);

        // Assert — if no exception, the contract is respected
        assertThat(dto.roles()).containsExactly("ADMIN", "USER");
    }
}

