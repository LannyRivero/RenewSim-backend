package com.renewsim.backend.role_service.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;

import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Contract test to ensure RoleRepositoryPort defines the expected methods and
 * signatures.
 */

class RoleRepositoryPortContractTest {

    @Test
    @DisplayName("should define save method returning Role")
    void shouldDefineSaveMethodReturningRole() throws NoSuchMethodException {
        var method = RoleRepositoryPort.class.getMethod("save", Role.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(Role.class);
    }

    @Test
    @DisplayName("should define findById method returning Optional<Role>")
    void shouldDefineFindByIdMethodReturningOptionalRole() throws NoSuchMethodException {
        var method = RoleRepositoryPort.class.getMethod("findById", Long.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(Optional.class);
    }

    @Test
    @DisplayName("should define findByName method returning Optional<Role>")
    void shouldDefineFindByNameMethodReturningOptionalRole() throws NoSuchMethodException {
        var method = RoleRepositoryPort.class.getMethod("findByName", RoleName.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(Optional.class);
    }

    @Test
    @DisplayName("should define findAll method returning List<Role>")
    void shouldDefineFindAllMethodReturningListRole() throws NoSuchMethodException {
        var method = RoleRepositoryPort.class.getMethod("findAll");
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("should define deleteById method returning void")
    void shouldDefineDeleteByIdMethodReturningVoid() throws NoSuchMethodException {
        var method = RoleRepositoryPort.class.getMethod("deleteById", Long.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(void.class);
    }

    @Test
    @DisplayName("should define countByName method returning long")
    void shouldDefineCountByNameMethodReturningLong() throws NoSuchMethodException {
        var method = RoleRepositoryPort.class.getMethod("countByName", RoleName.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(long.class);
    }

    @Test
    @DisplayName("should allow mocking and invocation of all methods")
    void shouldAllowMockingAndInvocationOfAllMethods() {
        RoleRepositoryPort repository = new RoleRepositoryPort() {
            @Override
            public Role save(Role role) {
                return new Role(RoleName.ADMIN);
            }

            @Override
            public Optional<Role> findById(Long id) {
                return Optional.of(new Role(RoleName.ADMIN));
            }

            @Override
            public Optional<Role> findByName(RoleName roleName) {
                return roleName == RoleName.ADMIN
                        ? Optional.of(new Role(RoleName.ADMIN))
                        : Optional.empty();
            }

            @Override
            public List<Role> findAll() {
                return List.of(new Role(RoleName.ADMIN), new Role(RoleName.USER));
            }

            @Override
            public void deleteById(Long id) {
                // simulate delete
            }

            @Override
            public long countByName(RoleName roleName) {
                return roleName == RoleName.ADMIN ? 1L : 0L;
            }
        };

        Role saved = repository.save(new Role(RoleName.ADMIN));
        assertThat(saved).isNotNull().extracting(Role::name).isEqualTo(RoleName.ADMIN);

        Optional<Role> foundById = repository.findById(1L);
        assertThat(foundById).isPresent();

        Optional<Role> foundByName = repository.findByName(RoleName.ADMIN);
        assertThat(foundByName).isPresent();

        List<Role> allRoles = repository.findAll();
        assertThat(allRoles).hasSize(2);

        repository.deleteById(1L); // no exception expected

        long count = repository.countByName(RoleName.ADMIN);
        assertThat(count).isEqualTo(1L);
    }
}
