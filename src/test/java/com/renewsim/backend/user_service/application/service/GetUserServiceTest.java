package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.InvalidUserDataException;
import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetUserServiceTest {

    @Mock

    private UserRepositoryPort userRepositoryPort;

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
                Set.of(RoleName.USER),
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
        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(sampleUser));

        UserResponse result = service.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("alice");
    }

    @Test
    @DisplayName("should throw UserNotFoundException 404 when user not found by id")
    void testUserNotFoundThrowsUserNotFoundException() {
        when(userRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with id 99 not found");
    }

    @Test
    @DisplayName("should throw UserNotFoundException 404 when id is null")
    void testGetUserByIdWithNullId() {
        when(userRepositoryPort.findById(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(null))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with id null not found");
    }

    @Test
    @DisplayName("should return 404 when id is negative")
    void testGetUserByIdWithNegativeId() {
        when(userRepositoryPort.findById(-1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserById(-1L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with id -1 not found");
    }

    // ---------------------------
    // getUserByUsernameOrEmail
    // ---------------------------
    @Test
    @DisplayName("should throw InvalidUserDataException when both username and email are null")
    void testGetUserByUsernameOrEmailBothNull() {
        assertThatThrownBy(() -> service.getUserByUsernameOrEmail(null, null))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Either username or email must be provided");
    }

    @Test
    @DisplayName("should return user when found by username")
    void testGetUserByUsername() {
        when(userRepositoryPort.findByUsername("alice")).thenReturn(Optional.of(sampleUser));

        UserResponse result = service.getUserByUsernameOrEmail("alice", null);

        assertThat(result.username()).isEqualTo("alice");
    }

    @Test
    @DisplayName("should return user when found by email")
    void testGetUserByEmail() {
        when(userRepositoryPort.findByEmail("alice@mail.com")).thenReturn(Optional.of(sampleUser));

        UserResponse result = service.getUserByUsernameOrEmail(null, "alice@mail.com");

        assertThat(result.email()).isEqualTo("alice@mail.com");
    }

    @Test
    @DisplayName("should throw 404 when user not found by username")
    void testUserNotFoundByUsername() {
        when(userRepositoryPort.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserByUsernameOrEmail("ghost", null))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with username 'ghost' not found");
    }

    @Test
    @DisplayName("should throw 404 when user not found by email")
    void testUserNotFoundByEmail() {
        when(userRepositoryPort.findByEmail("ghost@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getUserByUsernameOrEmail(null, "ghost@mail.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with email 'ghost@mail.com' not found");
    }

    // ---------------------------
    // getDomainUserByUsernameOrEmail
    // ---------------------------
    @Test
    @DisplayName("should return domain User when found by username")
    void testGetDomainUserByUsername() {
        when(userRepositoryPort.findByUsername("alice")).thenReturn(Optional.of(sampleUser));

        User result = service.getDomainUserByUsernameOrEmail("alice", null);

        assertThat(result.username()).isEqualTo("alice");
    }

    @Test
    @DisplayName("should throw 404 when domain user not found")
    void testGetDomainUserNotFound() {
        when(userRepositoryPort.findByEmail("unknown@mail.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDomainUserByUsernameOrEmail(null, "unknown@mail.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User with email 'unknown@mail.com' not found");
    }
}
