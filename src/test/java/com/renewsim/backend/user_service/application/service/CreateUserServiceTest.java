package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.user_service.application.port.out.ExistsUserPort;
import com.renewsim.backend.user_service.application.port.out.SaveUserPort;
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
    private SaveUserPort saveUserPort;

    @Mock
    private ExistsUserPort existsUserPort;

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

        User savedUser = new User(1L, "alice", "alice@mail.com", true, Set.of("USER"), null, null, "StrongPass1");

        when(existsUserPort.existsByUsernameOrEmail("alice", "alice@mail.com")).thenReturn(false);
        when(saveUserPort.saveUser(any(User.class))).thenReturn(savedUser);

        UserResponse result = service.createUser(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("alice");
        assertThat(result.email()).isEqualTo("alice@mail.com"); 
        assertThat(result.roles()).contains("USER");

        verify(saveUserPort).saveUser(any(User.class));
    }

    // ---------------------------
    // Duplicate user
    // ---------------------------
    @Test
    @DisplayName("should throw DataIntegrityViolationException when username or email already exists")
    void testDuplicateUserThrowsDataIntegrityViolationException() {
        UserCreateRequest request = new UserCreateRequest("bob", "bob@mail.com", "StrongPass1");

        when(existsUserPort.existsByUsernameOrEmail("bob", "bob@mail.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("already exists");
    }

    // ---------------------------
    // Persistence error
    // ---------------------------
    @Test
    @DisplayName("should throw DataIntegrityViolationException when save fails")
    void testPersistenceErrorThrowsDataIntegrityViolationException() {
        UserCreateRequest request = new UserCreateRequest("charlie", "charlie@mail.com", "StrongPass1");

        when(existsUserPort.existsByUsernameOrEmail("charlie", "charlie@mail.com")).thenReturn(false);
        when(saveUserPort.saveUser(any(User.class)))
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

        User savedUser = new User(2L, "diana", "diana@mail.com", true, Set.of("USER"), null, null, "StrongPass1");

        when(existsUserPort.existsByUsernameOrEmail("diana", "diana@mail.com")).thenReturn(false);
        when(saveUserPort.saveUser(any(User.class))).thenReturn(savedUser);

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

        User savedUser = new User(3L, "eve", "eve@mail.com", true, Set.of("USER"), null, null, "StrongPass1");

        when(existsUserPort.existsByUsernameOrEmail("eve", "eve@mail.com")).thenReturn(false);
        when(saveUserPort.saveUser(any(User.class))).thenReturn(savedUser);

        UserResponse result = service.createUser(request);

        assertThat(result.email()).isEqualTo("eve@mail.com");
        verify(saveUserPort).saveUser(any(User.class));
    }
}
