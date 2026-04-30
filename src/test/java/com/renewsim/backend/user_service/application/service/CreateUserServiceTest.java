package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.UserAlreadyExistsException;
import com.renewsim.backend.user_service.application.mapper.UserServiceMapper;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.domain.model.UserStatus;
import com.renewsim.backend.user_service.web.dto.UserCreateRequest;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
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

    private static final String VALID_HASH = new BCryptPasswordEncoder(12).encode("StrongPass1");

    @BeforeEach
    void setup() {
        lenient().when(passwordEncoder.encode(anyString())).thenReturn(VALID_HASH);
    }

    private User buildUser(Long id, String email) {
        return User.reconstitute(
                id,
                email,
                VALID_HASH,
                "John",
                null,
                UserStatus.ACTIVE,
                Set.of(RoleName.USER),
                LocalDateTime.now(),
                LocalDateTime.now(),
                true,
                LocalDateTime.now());
    }

    private UserResponse buildResponse(Long id, String email) {
        return new UserResponse(id, "alice", email, "Alice", null,
                "ACTIVE", Set.of("USER"), null, null);
    }

    @Test
    @DisplayName("should create valid user successfully")
    void testCreateValidUser() {
        UserCreateRequest request = new UserCreateRequest("alice", "alice@mail.com", "StrongPass1", null, null);
        User saved = buildUser(1L, "alice@mail.com");
        UserResponse response = buildResponse(1L, "alice@mail.com");

        when(userRepositoryPort.existsByUsername("alice")).thenReturn(false);
        when(userRepositoryPort.existsByEmail("alice@mail.com")).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        UserResponse result = service.createUser(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("alice@mail.com");
        verify(passwordEncoder).encode("StrongPass1");
        verify(userRepositoryPort).save(any(User.class));
    }

    @Test
    @DisplayName("should throw UserAlreadyExistsException when username already exists")
    void testDuplicateUserThrowsException() {
        UserCreateRequest request = new UserCreateRequest("bob", "bob@mail.com", "StrongPass1", null, null);

        when(userRepositoryPort.existsByUsername("bob")).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("already exists");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("should throw when save fails with DataIntegrityViolationException")
    void testPersistenceErrorThrows() {
        UserCreateRequest request = new UserCreateRequest("charlie", "charlie@mail.com", "StrongPass1", null, null);

        when(userRepositoryPort.existsByUsername("charlie")).thenReturn(false);
        when(userRepositoryPort.existsByEmail("charlie@mail.com")).thenReturn(false);
        when(userRepositoryPort.save(any(User.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("constraint"));

        assertThatThrownBy(() -> service.createUser(request))
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("should assign default USER role")
    void testUserGetsDefaultRole() {
        UserCreateRequest request = new UserCreateRequest("diana", "diana@mail.com", "StrongPass1", null, null);
        User saved = buildUser(2L, "diana@mail.com");
        UserResponse response = buildResponse(2L, "diana@mail.com");

        when(userRepositoryPort.existsByUsername("diana")).thenReturn(false);
        when(userRepositoryPort.existsByEmail("diana@mail.com")).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        UserResponse result = service.createUser(request);

        assertThat(result.roles()).containsExactly("USER");
    }

    @Test
    @DisplayName("should normalize email before persisting")
    void testEmailIsNormalized() {
        UserCreateRequest request = new UserCreateRequest("eve", "Eve@MAIL.COM", "StrongPass1", null, null);
        User saved = buildUser(3L, "eve@mail.com");
        UserResponse response = buildResponse(3L, "eve@mail.com");

        when(userRepositoryPort.existsByUsername("eve")).thenReturn(false);
        when(userRepositoryPort.existsByEmail("eve@mail.com")).thenReturn(false);
        when(userRepositoryPort.save(any(User.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        UserResponse result = service.createUser(request);

        assertThat(result.email()).isEqualTo("eve@mail.com");
    }
}