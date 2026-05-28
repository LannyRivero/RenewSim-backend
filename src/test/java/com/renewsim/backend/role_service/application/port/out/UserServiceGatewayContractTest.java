package com.renewsim.backend.role_service.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.renewsim.backend.role_service.application.dto.UserRolesUpdateRequest;

/**
 * Contract test to ensure UserServiceGateway defines the correct method signature
 * and can be mocked or invoked safely from the Role Service.
 */
class UserServiceGatewayContractTest {

    @Test
    @DisplayName("should define updateUserRoles method with correct signature")
    void shouldDefineUpdateUserRolesMethodWithCorrectSignature() throws NoSuchMethodException {
        var method = UserServiceGateway.class.getMethod("updateUserRoles", Long.class, UserRolesUpdateRequest.class);

        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(void.class);
    }

    @Test
    @DisplayName("should allow mocking and invocation of updateUserRoles with single roles list")
    void shouldAllowMockingAndInvocationOfUpdateUserRoles() {
        UserServiceGateway gateway = new UserServiceGateway() {
            @Override
            public void updateUserRoles(Long userId, UserRolesUpdateRequest request) {
                assertThat(userId).isPositive();
                assertThat(request).isNotNull();
                assertThat(request.roles()).isNotEmpty();
            }

            @Override
            public void assignRole(Long userId, Long roleId) {
                assertThat(userId).isPositive();
                assertThat(roleId).isPositive();
            }

            @Override
            public void removeRole(Long userId, Long roleId) {
                assertThat(userId).isPositive();
                assertThat(roleId).isPositive();
            }
        };

        UserRolesUpdateRequest dto = new UserRolesUpdateRequest(List.of("ADMIN", "USER"));

        gateway.updateUserRoles(42L, dto);
        gateway.assignRole(42L, 1L);
        gateway.removeRole(42L, 1L);

        assertThat(dto.roles()).containsExactly("ADMIN", "USER");
    }
}

