package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.shared.exception.UserAlreadyExistsException;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserCreateRequest;
import com.renewsim.backend.user_service.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private CreateUserService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ---------------------------
    // Happy path
    // ---------------------------
    @Test
    @DisplayName("should create valid user successfully")
    void testCreateValidUser() {
        UserCreateRequest request = new UserCreateRequest("Alice", "Alice@Mail.com", "StrongPass1");

        User savedUser = new User(1L, "alice", "alice@mail.com", true, Set.of(RoleName.USER), null, null, "StrongPass1");

        when(userRepositoryPort.existsByUsername("alice")).thenReturn(false);
        when(userRepositoryPort.existsByEmail("alice@mail.com")).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);

        UserResponse result = service.createUser(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("alice");
        assertThat(result.email()).isEqualTo("alice@mail.com");
        assertThat(result.roles()).contains("USER");

        verify(userRepositoryPort).save(any(User.class));
    }

    // ---------------------------
    // Duplicate user
    // ---------------------------
    @Test
    @DisplayName("should throw UserAlreadyExistsException when username or email already exists")
    void testDuplicateUserThrowsDataIntegrityViolationException() {
        UserCreateRequest request = new UserCreateRequest("bob", "bob@mail.com", "StrongPass1");

        when(userRepositoryPort.existsByUsername("bob")).thenReturn(true);
        when(userRepositoryPort.existsByEmail("bob@mail.com")).thenReturn(false);

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }

    // ---------------------------
    // Persistence error
    // ---------------------------
    @Test
    @DisplayName("should throw DataIntegrityViolationException when save fails")
    void testPersistenceErrorThrowsDataIntegrityViolationException() {
        UserCreateRequest request = new UserCreateRequest("charlie", "charlie@mail.com", "StrongPass1");

        when(userRepositoryPort.existsByUsername("charlie")).thenReturn(false);
        when(userRepositoryPort.existsByEmail("charlie@mail.com")).thenReturn(false);
        when(userRepositoryPort.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("constraint violation"));

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ---------------------------
    // Extra: roles
    // ---------------------------
    @Test
    @DisplayName("should assign default USER role when none provided")
    void testUserGetsDefaultRole() {
        UserCreateRequest request = new UserCreateRequest("diana", "Diana@Mail.com", "StrongPass1");

        User savedUser = new User(2L, "diana", "diana@mail.com", true, Set.of(RoleName.USER), null, null, "StrongPass1");

        when(userRepositoryPort.existsByUsername("diana")).thenReturn(false);
        when(userRepositoryPort.existsByEmail("diana@mail.com")).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);

        UserResponse result = service.createUser(request);

        assertThat(result.roles()).containsExactly("USER");
    }

    // ---------------------------
    // Extra: email normalization
    // ---------------------------
    @Test
    @DisplayName("should normalize email before persisting")
    void testEmailIsNormalized() {
        UserCreateRequest request = new UserCreateRequest("eve", " Eve@MAIL.COM ", "StrongPass1");

        User savedUser = new User(3L, "eve", "eve@mail.com", true, Set.of(RoleName.USER), null, null, "StrongPass1");

        when(userRepositoryPort.existsByUsername("eve")).thenReturn(false);
        when(userRepositoryPort.existsByEmail("eve@mail.com")).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);

        UserResponse result = service.createUser(request);

        assertThat(result.email()).isEqualTo("eve@mail.com");
        verify(userRepositoryPort).save(any(User.class));
    }
}
