package com.renewsim.backend.auth_service.infrastructure.persistence;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.role.Role;
import com.renewsim.backend.role.RoleName;
import com.renewsim.backend.role.RoleRepository;
import com.renewsim.backend.user.User;
import com.renewsim.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaUserAccountGatewayTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;

    @InjectMocks
    private JpaUserAccountGatewayTest gateway;

    private Role roleUser;

    @BeforeEach
    void setUp() {
        roleUser = new Role();
        roleUser.setName(RoleName.USER);
    }

    @Test
    @DisplayName("findByUsername() should map entity to UserSnapshot when user exists")
    void testShouldReturnSnapshot_WhenUserExists() {
        User entity = new User("john", "$hash", Set.of(roleUser));
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(entity));

        Optional<UserSnapshot> opt = gateway.findByUsername("john");

        assertThat(opt).isPresent();
        UserSnapshot snap = opt.get();
        assertThat(snap.username()).isEqualTo("john");
        assertThat(snap.passwordHash()).isEqualTo("$hash");
        assertThat(snap.roles()).containsExactly(RoleName.USER);
        verify(userRepository).findByUsername("john");
    }

    @Test
    @DisplayName("findByUsername() should return empty when user does not exist")
    void testShouldReturnEmpty_WhenUserNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        Optional<UserSnapshot> opt = gateway.findByUsername("missing");

        assertThat(opt).isEmpty();
        verify(userRepository).findByUsername("missing");
    }

    @Test
    @DisplayName("existsByUsername() should delegate to repository")
    void testShouldDelegateExistsByUsername() {
        when(userRepository.existsByUsername("john")).thenReturn(true);

        boolean exists = gateway.existsByUsername("john");

        assertThat(exists).isTrue();
        verify(userRepository).existsByUsername("john");
    }

    @Test
    @DisplayName("createUser() should resolve roles and save a new user")
    void testShouldCreateUser_WhenRolesExist() {
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(roleUser));

        gateway.createUser("john", "$hash", Set.of(RoleName.USER));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getUsername()).isEqualTo("john");
        assertThat(saved.getPassword()).isEqualTo("$hash");
        assertThat(saved.getRoles()).extracting(Role::getName).containsExactly(RoleName.USER);

        verify(roleRepository).findByName(RoleName.USER);
    }

    @Test
    @DisplayName("createUser() should throw when a role does not exist")
    void testShouldThrow_WhenRoleNotFound() {
        when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                gateway.createUser("john", "$hash", Set.of(RoleName.ADMIN))
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Role not found: ADMIN");

        verify(roleRepository).findByName(RoleName.ADMIN);
        verifyNoInteractions(userRepository); 
    }
}

