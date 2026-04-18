package com.renewsim.backend.auth_service.infrastructure.persistence;

import com.renewsim.backend.auth_service.infrastructure.client.UserServiceClient;
import com.renewsim.backend.auth_service.web.dto.ExternalUserSnapshot;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.dto.OperationResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class HttpUserAccountGatewayTest {

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private HttpUserAccountGateway gateway;

    @Test
    @DisplayName("findByUsername() should map ExternalUserSnapshot -> UserSnapshot")
    void testFindByUsername() {
        var external = new ExternalUserSnapshot(1L, "john", "$hash", "john@example.com", Set.of("USER"));
        when(userServiceClient.getCredentials("john", null)).thenReturn(OperationResponse.ok(external, "Found"));

        Optional<UserSnapshot> opt = gateway.findByUsername("john");

        assertThat(opt).isPresent();
        UserSnapshot snap = opt.get();
        assertThat(snap.username()).isEqualTo("john");
        assertThat(snap.passwordHash()).isEqualTo("$hash");
        assertThat(snap.email()).isEqualTo("john@example.com");
        assertThat(snap.roles()).containsExactly(RoleName.USER);
        assertThat(snap.enabled()).isTrue();
    }

    @Test
    @DisplayName("findByUsername() should return empty when user does not exist")
    void testFindByUsername_WhenUserNotFound() {
        when(userServiceClient.getCredentials("missing", null)).thenReturn(null);

        Optional<UserSnapshot> opt = gateway.findByUsername("missing");

        assertThat(opt).isEmpty();
        verify(userServiceClient).getCredentials("missing", null);
    }

    @Test
    @DisplayName("createUser() should delegate to UserServiceClient and map response")
    void testCreateUser() {
        String rawPassword = "secret";
        String hashedPassword = "$2a$10$hashed";

        var externalCreated = new ExternalUserSnapshot(1L, "john", hashedPassword, "john@example.com", Set.of("USER"));
        when(userServiceClient.createUser(any())).thenReturn(OperationResponse.ok(externalCreated, "Created"));

        UserSnapshot result = gateway.createUser("john", rawPassword, "john@example.com", Set.of(RoleName.USER));

        assertThat(result.username()).isEqualTo("john");
        assertThat(result.passwordHash()).isEqualTo(hashedPassword);
        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.roles()).containsExactly(RoleName.USER);
        assertThat(result.enabled()).isTrue();

        verify(userServiceClient).createUser(argThat(request -> request.username().equals("john") &&
                request.password().equals(rawPassword) &&
                request.email().equals("john@example.com")));
    }
}
