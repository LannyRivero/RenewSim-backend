package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.ConflictException;
import com.renewsim.backend.shared.exception.InvalidUserDataException;
import com.renewsim.backend.shared.exception.ResourceNotFoundException;
import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.application.port.out.RoleCatalogPort;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.web.dto.RoleSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRoleAssignmentServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private RoleCatalogPort roleCatalogPort;

    private UserRoleAssignmentService service;

    @BeforeEach
    void setUp() {
        service = new UserRoleAssignmentService(userRepositoryPort, roleCatalogPort);
    }

    @Test
    @DisplayName("assignRole should add the resolved role to the user")
    void assignRole_addsResolvedRole() {
        User user = sampleUser(Set.of(RoleName.USER));
        when(userRepositoryPort.findById(7L)).thenReturn(Optional.of(user));
        when(roleCatalogPort.findById(2L)).thenReturn(Optional.of(new RoleSnapshot(2L, "ANALYST")));

        service.assignRole(7L, 2L);

        assertThat(user.getRoles()).containsExactlyInAnyOrder(RoleName.USER, RoleName.ANALYST);
        verify(userRepositoryPort).save(user);
    }

    @Test
    @DisplayName("assignRole should throw 404 when user does not exist")
    void assignRole_throwsWhenUserMissing() {
        when(userRepositoryPort.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignRole(7L, 2L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with id=7 not found");

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("assignRole should throw 404 when role does not exist")
    void assignRole_throwsWhenRoleMissing() {
        User user = sampleUser(Set.of(RoleName.USER));
        when(userRepositoryPort.findById(7L)).thenReturn(Optional.of(user));
        when(roleCatalogPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignRole(7L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role with id=99 not found");

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("assignRole should reject infrastructure roles")
    void assignRole_rejectsInfrastructureRole() {
        User user = sampleUser(Set.of(RoleName.USER));
        when(userRepositoryPort.findById(7L)).thenReturn(Optional.of(user));
        when(roleCatalogPort.findById(4L)).thenReturn(Optional.of(new RoleSnapshot(4L, "SERVICE_AUTH")));

        assertThatThrownBy(() -> service.assignRole(7L, 4L))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Infrastructure roles cannot be assigned to users");

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("removeRole should remove the assigned role")
    void removeRole_removesAssignedRole() {
        User user = sampleUser(Set.of(RoleName.USER, RoleName.ANALYST));
        when(userRepositoryPort.findById(7L)).thenReturn(Optional.of(user));
        when(roleCatalogPort.findById(2L)).thenReturn(Optional.of(new RoleSnapshot(2L, "ANALYST")));

        service.removeRole(7L, 2L);

        assertThat(user.getRoles()).containsExactly(RoleName.USER);
        verify(userRepositoryPort).save(user);
    }

    @Test
    @DisplayName("removeRole should throw 404 when assignment does not exist")
    void removeRole_throwsWhenAssignmentMissing() {
        User user = sampleUser(Set.of(RoleName.USER));
        when(userRepositoryPort.findById(7L)).thenReturn(Optional.of(user));
        when(roleCatalogPort.findById(2L)).thenReturn(Optional.of(new RoleSnapshot(2L, "ANALYST")));

        assertThatThrownBy(() -> service.removeRole(7L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role assignment not found");

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("removeRole should reject removing the last ADMIN assignment")
    void removeRole_rejectsRemovingLastAdmin() {
        User user = sampleUser(Set.of(RoleName.ADMIN));
        when(userRepositoryPort.findById(7L)).thenReturn(Optional.of(user));
        when(roleCatalogPort.findById(1L)).thenReturn(Optional.of(new RoleSnapshot(1L, "ADMIN")));
        when(userRepositoryPort.countByRole(RoleName.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.removeRole(7L, 1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("at least one administrator must remain");

        assertThat(user.getRoles()).containsExactly(RoleName.ADMIN);
        verify(userRepositoryPort, never()).save(any());
    }

    private static User sampleUser(Set<RoleName> roles) {
        return User.reconstitute(
                7L,
                "alice@renewsim.com",
                "$2a$10$7EqJtq98hPqEX7fNZaFWoOHi8wA1Q9Y/4I2oXeB9lnAaPD75eQxPe",
                "Alice",
                null,
                com.renewsim.backend.user_service.domain.model.UserStatus.ACTIVE,
                roles,
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                true,
                java.time.LocalDateTime.now());
    }
}
