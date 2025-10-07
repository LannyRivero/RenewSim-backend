package com.renewsim.backend.role_service.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;

import com.renewsim.backend.role_service.domain.model.RoleName;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract test to ensure that the ExistsRoleUseCase interface
 * defines the correct method signature and behaves as expected when mocked.
 */
class ExistsRoleUseCaseContractTest {

    @Test
    @DisplayName("should define existsByName method with correct signature")
    void shouldDefineExistsByNameMethodWithCorrectSignature() throws NoSuchMethodException {
        var method = ExistsRoleUseCase.class.getMethod("existsByName", RoleName.class);

        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("should allow mocking and invocation of existsByName method")
    void shouldAllowMockingAndInvocationOfExistsByName() {
        ExistsRoleUseCase useCase = roleName -> roleName == RoleName.ADMIN;

        boolean existsAdmin = useCase.existsByName(RoleName.ADMIN);
        boolean existsUser = useCase.existsByName(RoleName.USER);

        assertThat(existsAdmin).isTrue();
        assertThat(existsUser).isFalse();
    }
}
