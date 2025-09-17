package com.renewsim.backend.user_service.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.renewsim.backend.shared.exception.InvalidUserDataException;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExistsUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private ExistsUserService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("should return true when user exists by username")
    void testExistsUserByUsernameReturnsTrue() {
        when(userRepositoryPort.existsByUsername("alice")).thenReturn(true);

        boolean result = service.existsByUsernameOrEmail("alice", null);

        assertThat(result).isTrue();
        verify(userRepositoryPort).existsByUsername("alice");
    }

    @Test
    @DisplayName("should return true when user exists by email")
    void testExistsUserByEmailReturnsTrue() {
        when(userRepositoryPort.existsByEmail("alice@mail.com")).thenReturn(true);

        boolean result = service.existsByUsernameOrEmail(null, "alice@mail.com");

        assertThat(result).isTrue();
        verify(userRepositoryPort).existsByEmail("alice@mail.com");
    }

    @Test
    @DisplayName("should return false when user does not exist by username")
    void testExistsUserByUsernameReturnsFalse() {
        when(userRepositoryPort.existsByUsername("bob")).thenReturn(false);

        boolean result = service.existsByUsernameOrEmail("bob", null);

        assertThat(result).isFalse();
        verify(userRepositoryPort).existsByUsername("bob");
    }

    @Test
    @DisplayName("should throw InvalidUserDataException when both username and email are null")
    void testExistsUserThrowsWhenBothNull() {
        assertThatThrownBy(() -> service.existsByUsernameOrEmail(null, null))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Either username or email must be provided");
    }

    @Test
    @DisplayName("should propagate exception thrown by UserRepositoryPort")
    void testExistsUserPropagatesException() {
        when(userRepositoryPort.existsByUsername("charlie"))
                .thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> service.existsByUsernameOrEmail("charlie", null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }
}

