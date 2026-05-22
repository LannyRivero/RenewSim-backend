package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.ConflictException;
import com.renewsim.backend.shared.exception.InvalidUserDataException;
import com.renewsim.backend.user_service.application.port.out.RoleCatalogPort;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.web.dto.UpdateUserRolesRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserRolesServiceTest {

    private static final String VALID_BCRYPT = new BCryptPasswordEncoder(12).encode("password123");

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private RoleCatalogPort roleCatalogPort;

    private UpdateUserRolesService service;

    @BeforeEach
    void setUp() {
        service = new UpdateUserRolesService(userRepositoryPort, roleCatalogPort);
    }

    @Test
    @DisplayName("updateUserRoles should replace existing roles without failing during iteration")
    void updateUserRoles_replacesRolesSafely() {
        User user = User.reconstitute(
                6L,
                "user@renewsim.com",
                VALID_BCRYPT,
                "User Test",
                "600000000",
                com.renewsim.backend.user_service.domain.model.UserStatus.ACTIVE,
                Set.of(RoleName.USER, RoleName.ADMIN),
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                true,
                java.time.LocalDateTime.now());

        when(userRepositoryPort.findById(6L)).thenReturn(Optional.of(user));
        when(roleCatalogPort.existsByName(RoleName.USER)).thenReturn(true);
        when(userRepositoryPort.countByRole(RoleName.ADMIN)).thenReturn(2L);
        when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.updateUserRoles(6L, new UpdateUserRolesRequestDTO(java.util.List.of("USER")));

        assertThat(user.getRoles()).containsExactly(RoleName.USER);
        verify(userRepositoryPort).save(user);
    }

    @Test
    @DisplayName("updateUserRoles should reject removing the last ADMIN assignment")
    void updateUserRoles_rejectsRemovingLastAdmin() {
        User user = User.reconstitute(
                6L,
                "admin@renewsim.com",
                VALID_BCRYPT,
                "Admin Test",
                "600000000",
                com.renewsim.backend.user_service.domain.model.UserStatus.ACTIVE,
                Set.of(RoleName.ADMIN),
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                true,
                java.time.LocalDateTime.now());

        when(userRepositoryPort.findById(6L)).thenReturn(Optional.of(user));
        when(roleCatalogPort.existsByName(RoleName.USER)).thenReturn(true);
        when(userRepositoryPort.countByRole(RoleName.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.updateUserRoles(6L, new UpdateUserRolesRequestDTO(java.util.List.of("USER"))))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("at least one administrator must remain");

        assertThat(user.getRoles()).containsExactly(RoleName.ADMIN);
        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUserRoles should reject infrastructure roles")
    void updateUserRoles_rejectsInfrastructureRoles() {
        User user = User.reconstitute(
                6L,
                "user@renewsim.com",
                VALID_BCRYPT,
                "User Test",
                "600000000",
                com.renewsim.backend.user_service.domain.model.UserStatus.ACTIVE,
                Set.of(RoleName.USER),
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                true,
                java.time.LocalDateTime.now());

        when(userRepositoryPort.findById(6L)).thenReturn(Optional.of(user));
        when(roleCatalogPort.existsByName(RoleName.USER)).thenReturn(true);

        assertThatThrownBy(() -> service.updateUserRoles(6L,
                new UpdateUserRolesRequestDTO(java.util.List.of("USER", "SERVICE_AUTH"))))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Infrastructure roles cannot be assigned to users");

        assertThat(user.getRoles()).containsExactly(RoleName.USER);
        verify(userRepositoryPort, never()).save(any(User.class));
    }
}
