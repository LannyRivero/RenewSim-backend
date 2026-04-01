package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.UserAlreadyExistsException;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.UserCreateRequest;
import com.renewsim.backend.user_service.dto.UserResponse;
import com.renewsim.backend.user_service.infrastructure.mapper.UserServiceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private UserServiceMapper mapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateUserService service;

    @BeforeEach
    void setup() {
  
        lenient().when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    }

    // ---------------------------
    // Happy path
    // ---------------------------
    @Test
    @DisplayName("should create valid user successfully")
    void testCreateValidUser() {
        UserCreateRequest request = new UserCreateRequest("Alice", "Alice@Mail.com", "StrongPass1");

        User savedUser = new User(1L, "alice", "alice@mail.com", true, Set.of(RoleName.USER), null, null, "encodedPassword");
        UserResponse response = new UserResponse(1L, "alice", "alice@mail.com", true, Set.of("USER"), null, null);

        when(userRepositoryPort.existsByUsername("alice")).thenReturn(false);
        when(userRepositoryPort.existsByEmail("alice@mail.com")).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);
        when(mapper.toResponse(savedUser)).thenReturn(response);

        UserResponse result = service.createUser(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("alice");
        assertThat(result.email()).isEqualTo("alice@mail.com");
        assertThat(result.roles()).contains("USER");

        verify(passwordEncoder).encode("StrongPass1");
        verify(userRepositoryPort).save(any(User.class));
        verify(mapper).toResponse(savedUser);
    }

    // ---------------------------
    // Duplicate user
    // ---------------------------
    @Test
    @DisplayName("should throw UserAlreadyExistsException when username or email already exists")
    void testDuplicateUserThrowsDataIntegrityViolationException() {
        UserCreateRequest request = new UserCreateRequest("bob", "bob@mail.com", "StrongPass1");

        when(userRepositoryPort.existsByUsername("bob")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepositoryPort, never()).save(any());
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

        verify(passwordEncoder).encode("StrongPass1");
    }

    // ---------------------------
    // Extra: roles
    // ---------------------------
    @Test
    @DisplayName("should assign default USER role when none provided")
    void testUserGetsDefaultRole() {
        UserCreateRequest request = new UserCreateRequest("diana", "Diana@Mail.com", "StrongPass1");

        User savedUser = new User(2L, "diana", "diana@mail.com", true, Set.of(RoleName.USER), null, null, "encodedPassword");
        UserResponse response = new UserResponse(2L, "diana", "diana@mail.com", true, Set.of("USER"), null, null);

        when(userRepositoryPort.existsByUsername("diana")).thenReturn(false);
        when(userRepositoryPort.existsByEmail("diana@mail.com")).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);
        when(mapper.toResponse(savedUser)).thenReturn(response);

        UserResponse result = service.createUser(request);

        assertThat(result.roles()).containsExactly("USER");
        verify(passwordEncoder).encode("StrongPass1");
    }

    // ---------------------------
    // Extra: email normalization
    // ---------------------------
    @Test
    @DisplayName("should normalize email before persisting")
    void testEmailIsNormalized() {
        UserCreateRequest request = new UserCreateRequest("eve", " Eve@MAIL.COM ", "StrongPass1");

        User savedUser = new User(3L, "eve", "eve@mail.com", true, Set.of(RoleName.USER), null, null, "encodedPassword");
        UserResponse response = new UserResponse(3L, "eve", "eve@mail.com", true, Set.of("USER"), null, null);

        when(userRepositoryPort.existsByUsername("eve")).thenReturn(false);
        when(userRepositoryPort.existsByEmail("eve@mail.com")).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenReturn(savedUser);
        when(mapper.toResponse(savedUser)).thenReturn(response);

        UserResponse result = service.createUser(request);

        assertThat(result.email()).isEqualTo("eve@mail.com");
        verify(passwordEncoder).encode("StrongPass1");
        verify(userRepositoryPort).save(any(User.class));
    }
}