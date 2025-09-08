package com.renewsim.backend.auth_service.infrastructure.persistence;

import com.renewsim.backend.auth_service.infrastructure.client.UserServiceClient;
import com.renewsim.backend.auth_service.web.dto.ExternalUserSnapshot;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.role.RoleName;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class HttpUserAccountGatewayTest {

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private HttpUserAccountGateway gateway;

    @Test
    @DisplayName("findByUsername() should map ExternalUserSnapshot → UserSnapshot")
    void testFindByUsername() {
        var external = new ExternalUserSnapshot("john", "$hash", Set.of("USER"));
        when(userServiceClient.findByUsername("john")).thenReturn(external);

        Optional<UserSnapshot> opt = gateway.findByUsername("john");

        assertThat(opt).isPresent();
        UserSnapshot snap = opt.get();
        assertThat(snap.username()).isEqualTo("john");
        assertThat(snap.passwordHash()).isEqualTo("$hash");
        assertThat(snap.roles()).containsExactly(RoleName.USER);
    }

    @Test
    @DisplayName("findByUsername() should return empty when user does not exist")
    void testFindByUsername_WhenUserNotFound() {
        when(userServiceClient.findByUsername("missing")).thenReturn(null);

        Optional<UserSnapshot> opt = gateway.findByUsername("missing");

        assertThat(opt).isEmpty();
        verify(userServiceClient).findByUsername("missing");
    }

    @Test
    @DisplayName("existsByUsername() should delegate to UserServiceClient")
    void testExistsByUsername() {
        when(userServiceClient.existsByUsername("john")).thenReturn(true);

        boolean exists = gateway.existsByUsername("john");

        assertThat(exists).isTrue();
        verify(userServiceClient).existsByUsername("john");
    }

    @Test
    @DisplayName("createUser() should hash password and call UserServiceClient with mapped snapshot")
    void testCreateUser() {
        // Given
        String rawPassword = "secret";
        String hashedPassword = "$2a$10$hashed";
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);

        var externalCreated = new ExternalUserSnapshot("john", hashedPassword, Set.of("USER"));
        when(userServiceClient.createUser(any(ExternalUserSnapshot.class))).thenReturn(externalCreated);

        // When
        UserSnapshot result = gateway.createUser("john", rawPassword, Set.of(RoleName.USER));

        // Then
        assertThat(result.username()).isEqualTo("john");
        assertThat(result.passwordHash()).isEqualTo(hashedPassword);
        assertThat(result.roles()).containsExactly(RoleName.USER);

        verify(passwordEncoder).encode(rawPassword);
        verify(userServiceClient).createUser(argThat(snapshot -> snapshot.username().equals("john") &&
                snapshot.passwordHash().equals(hashedPassword) &&
                snapshot.roles().contains("USER")));
    }
}
