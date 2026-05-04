package com.renewsim.backend.auth_service.infrastructure.persistence;

import com.renewsim.backend.auth_service.infrastructure.client.UserServiceClient;
import com.renewsim.backend.auth_service.infrastructure.client.ExternalUserSnapshot;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.domain.model.AuthUserStatus;
import com.renewsim.backend.auth_service.infrastructure.mapper.UserSnapshotMapper;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.dto.OperationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HttpUserAccountGatewayTest {

        @Mock
        private UserServiceClient userServiceClient;

        @Mock
        private UserSnapshotMapper userSnapshotMapper;

        private HttpUserAccountGateway gateway;

        @BeforeEach
        void setUp() {
                gateway = new HttpUserAccountGateway(userServiceClient, userSnapshotMapper);

        }

        @Test
        @DisplayName("findByUsername() -> mapea ExternalUserSnapshot a UserSnapshot correctamente")
        void findByUsername_mapsSnapshotCorrectly() {
                var external = new ExternalUserSnapshot(
                                1L, "john", "John Doe", "$hash", "john@example.com",
                                Set.of("USER"), "ACTIVE", Boolean.TRUE);

                when(userServiceClient.getCredentials("john", null))
                                .thenReturn(OperationResponse.ok(external, "Found"));

                when(userSnapshotMapper.toSnapshot(external)).thenReturn(
                                UserSnapshot.active(
                                                external.id(),
                                                external.username(),
                                                external.fullName(),
                                                external.passwordHash(),
                                                external.email(),
                                                Set.of(RoleName.USER)));

                Optional<UserSnapshot> opt = gateway.findByUsername("john");

                assertThat(opt).isPresent();
                UserSnapshot snap = opt.get();
                assertThat(snap.username()).isEqualTo("john");
                assertThat(snap.fullName()).isEqualTo("John Doe");
                assertThat(snap.passwordHash()).isEqualTo("$hash");
                assertThat(snap.email()).isEqualTo("john@example.com");
                assertThat(snap.roles()).containsExactly(RoleName.USER);
                assertThat(snap.status()).isEqualTo(AuthUserStatus.ACTIVE);
                assertThat(snap.enabled()).isTrue();
        }

        @Test
        @DisplayName("findByUsername() -> devuelve empty cuando la respuesta es null")
        void findByUsername_nullResponse_returnsEmpty() {
                when(userServiceClient.getCredentials("missing", null)).thenReturn(null);
                when(userSnapshotMapper.toSnapshot(null)).thenReturn(null);

                Optional<UserSnapshot> opt = gateway.findByUsername("missing");

                assertThat(opt).isEmpty();
                verify(userServiceClient).getCredentials("missing", null);
        }

        @Test
        @DisplayName("createUser() -> delega en UserServiceClient y mapea la respuesta")
        void createUser_delegatesAndMapsResponse() {
                var externalCreated = new ExternalUserSnapshot(
                                1L, "john.doe", "John Doe", "$2a$10$hashed",
                                "john@example.com", Set.of("USER"), "INACTIVE", Boolean.FALSE);

                when(userServiceClient.createUser(any()))
                                .thenReturn(OperationResponse.ok(externalCreated, "Created"));

                when(userSnapshotMapper.toSnapshot(externalCreated)).thenReturn(
                                UserSnapshot.disabled(
                                                externalCreated.id(),
                                                externalCreated.username(),
                                                externalCreated.fullName(),
                                                externalCreated.passwordHash(),
                                                externalCreated.email(),
                                                Set.of(RoleName.USER)));

                UserSnapshot result = gateway.createUser(
                                "john.doe", "John Doe", "secret", "john@example.com", Set.of(RoleName.USER));

                assertThat(result.fullName()).isEqualTo("John Doe");
                assertThat(result.email()).isEqualTo("john@example.com");
                assertThat(result.roles()).containsExactly(RoleName.USER);
                assertThat(result.status()).isEqualTo(AuthUserStatus.INACTIVE);
                assertThat(result.enabled()).isFalse();

                verify(userServiceClient).createUser(argThat(req -> req.email().equals("john@example.com") &&
                                req.fullName().equals("John Doe") &&
                                req.password().equals("secret")));
        }

        @Test
        @DisplayName("existsByEmail() -> devuelve true cuando el servicio confirma existencia")
        void existsByEmail_returnsTrue_whenUserExists() {
                when(userServiceClient.existsByUsernameOrEmail(null, "john@example.com"))
                                .thenReturn(OperationResponse.ok(true, "exists"));

                assertThat(gateway.existsByEmail("john@example.com")).isTrue();

                verifyNoInteractions(userSnapshotMapper);
        }

        @Test
        @DisplayName("existsByEmail() -> devuelve false cuando el servicio retorna false")
        void existsByEmail_returnsFalse_whenUserNotExists() {
                when(userServiceClient.existsByUsernameOrEmail(null, "new@example.com"))
                                .thenReturn(OperationResponse.ok(false, "not found"));

                assertThat(gateway.existsByEmail("new@example.com")).isFalse();

                verifyNoInteractions(userSnapshotMapper);
        }
}