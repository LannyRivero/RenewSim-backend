package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.user_service.application.port.out.LoadUserPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetUserServiceTest {

    @Mock
    private LoadUserPort loadUserPort;

    @InjectMocks
    private GetUserService service;

    private User sampleUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        sampleUser = new User(
                1L,
                "alice",
                "alice@mail.com",
                true,
                Set.of("USER"),
                null,
                null,
                "StrongPass1");
    }

    // ---------------------------
    // getUserById
    // ---------------------------
    @Test
    @DisplayName("should return UserResponse when user is found by id")
    void testUserFoundById() {
        when(loadUserPort.loadUserById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponse result = service.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("alice");
    }

    @Test
    @DisplayName("should throw ResponseStatusException 404 when user not found by id")
    void testUserNotFoundThrowsResponseStatusException() {
        when(loadUserPort.loadUserById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }

    @Test
    @DisplayName("should throw ResponseStatusException 404 when id is null")
    void testGetUserByIdWithNullId() {
        when(loadUserPort.loadUserById(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }

    @Test
    @DisplayName("should return 404 when id is negative")
    void testGetUserByIdWithNegativeId() {
        when(loadUserPort.loadUserById(-1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(-1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }

    // ---------------------------
    // getUserByUsernameOrEmail
    // ---------------------------
    @Test
    @DisplayName("should throw IllegalArgumentException when both username and email are null")
    void testGetUserByUsernameOrEmailBothNull() {
        assertThatThrownBy(() -> service.getUserByUsernameOrEmail(null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Either username or email must be provided");
    }

    @Test
    @DisplayName("should return user when found by username")
    void testGetUserByUsername() {
        when(loadUserPort.loadUserByUsername("alice")).thenReturn(Optional.of(sampleUser));

        UserResponse result = service.getUserByUsernameOrEmail("alice", null);

        assertThat(result.username()).isEqualTo("alice");
    }

    @Test
    @DisplayName("should return user when found by email")
    void testGetUserByEmail() {
        when(loadUserPort.loadUserByEmail("alice@mail.com")).thenReturn(Optional.of(sampleUser));

        UserResponse result = service.getUserByUsernameOrEmail(null, "alice@mail.com");

        assertThat(result.email()).isEqualTo("alice@mail.com");
    }

    @Test
    @DisplayName("should throw 404 when user not found by username")
    void testUserNotFoundByUsername() {
        when(loadUserPort.loadUserByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserByUsernameOrEmail("ghost", null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }

    @Test
    @DisplayName("should throw 404 when user not found by email")
    void testUserNotFoundByEmail() {
        when(loadUserPort.loadUserByEmail("ghost@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserByUsernameOrEmail(null, "ghost@mail.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }

    // ---------------------------
    // getDomainUserByUsernameOrEmail
    // ---------------------------
    @Test
    @DisplayName("should return domain User when found by username")
    void testGetDomainUserByUsername() {
        when(loadUserPort.loadUserByUsername("alice")).thenReturn(Optional.of(sampleUser));

        User result = service.getDomainUserByUsernameOrEmail("alice", null);

        assertThat(result.username()).isEqualTo("alice");
    }

    @Test
    @DisplayName("should throw 404 when domain user not found")
    void testGetDomainUserNotFound() {
        when(loadUserPort.loadUserByEmail("unknown@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDomainUserByUsernameOrEmail(null, "unknown@mail.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404 NOT_FOUND");
    }
}
