package com.renewsim.backend.role_service.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;

import com.renewsim.backend.role_service.web.dto.RoleDTO;
import com.renewsim.backend.shared.common.application.port.in.GetAllUseCase;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract test to ensure that GetRolesUseCase correctly extends GetAllUseCase<RoleDTO>
 * and defines a method getAll() returning a list of RoleDTO.
 */
class GetRolesUseCaseContractTest {

    @Test
    @DisplayName("should extend GetAllUseCase with correct generic type RoleDTO")
    void shouldExtendGetAllUseCaseWithCorrectGenericType() {
        Class<?>[] interfaces = GetRolesUseCase.class.getInterfaces();

        assertThat(interfaces).isNotEmpty();
        assertThat(interfaces).contains(GetAllUseCase.class);
    }

    @Test
    @DisplayName("should define getAll method with correct signature")
    void shouldDefineGetAllMethodWithCorrectSignature() throws NoSuchMethodException {
        var method = GetRolesUseCase.class.getMethod("getAll");
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("should allow mocking and invocation of getAll method")
    void shouldAllowMockingAndInvocationOfGetAll() {
        GetRolesUseCase useCase = () -> List.of(
                new RoleDTO(1L, "ADMIN"),
                new RoleDTO(2L, "USER")
        );

        List<RoleDTO> roles = useCase.getAll();

        assertThat(roles)
                .isNotEmpty()
                .hasSize(2)
                .extracting(RoleDTO::name)
                .containsExactly("ADMIN", "USER");
    }
}

