package com.renewsim.backend.role_service.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;

import com.renewsim.backend.role_service.application.result.RoleDeletionResultDTO;
import com.renewsim.backend.shared.common.application.port.in.DeleteUseCase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract test to ensure that DeleteRoleUseCase extends DeleteUseCase<Long,
 * RoleDeletionResultDTO>
 * and can be invoked with a Long ID returning a proper result DTO.
 */
class DeleteRoleUseCaseContractTest {

    @Test
    @DisplayName("should extend DeleteUseCase with correct generic parameters")
    void shouldExtendDeleteUseCaseWithCorrectGenerics() {
        Class<?>[] interfaces = DeleteRoleUseCase.class.getInterfaces();

        assertThat(interfaces)
                .isNotEmpty()
                .contains(DeleteUseCase.class);
    }

    @Test
    @DisplayName("should allow mocking and invocation of delete method returning RoleDeletionResultDTO")
    void shouldAllowMockingAndInvocationOfDelete() {
        DeleteRoleUseCase useCase = id -> new RoleDeletionResultDTO(
                id,
                true,
                "Role deleted successfully");

        RoleDeletionResultDTO result = useCase.delete(10L);

        assertThat(result).isNotNull();
        assertThat(result.roleId()).isEqualTo(10L);
        assertThat(result.success()).isTrue();
        assertThat(result.message()).isEqualTo("Role deleted successfully");
    }

 @Test
@DisplayName("should define delete method inherited from DeleteUseCase with correct generic type parameters")
void shouldDefineDeleteMethodWithCorrectSignature() throws NoSuchMethodException {
    var method = DeleteRoleUseCase.class.getMethod("delete", Object.class);

    assertThat(method).isNotNull();
    assertThat(method.getReturnType()).isEqualTo(Object.class);

    var genericInterfaces = DeleteRoleUseCase.class.getGenericInterfaces();
    var parameterizedType = (java.lang.reflect.ParameterizedType) genericInterfaces[0];

    assertThat(parameterizedType.getActualTypeArguments()[0]).isEqualTo(Long.class);
    assertThat(parameterizedType.getActualTypeArguments()[1]).isEqualTo(RoleDeletionResultDTO.class);
}


}
