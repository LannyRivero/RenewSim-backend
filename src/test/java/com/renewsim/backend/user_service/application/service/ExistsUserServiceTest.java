package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.user_service.application.port.out.ExistsUserPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExistsUserServiceTest {

    @Mock
    private ExistsUserPort existsUserPort;

    @InjectMocks
    private ExistsUserService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("should return true when user exists by username or email")
    void testExistsUserReturnsTrue() {
        when(existsUserPort.existsByUsernameOrEmail("alice", "alice@mail.com")).thenReturn(true);

        boolean result = service.existsByUsernameOrEmail("alice", "alice@mail.com");

        assertThat(result).isTrue();
        verify(existsUserPort).existsByUsernameOrEmail("alice", "alice@mail.com");
    }

    @Test
    @DisplayName("should return false when user does not exist by username or email")
    void testExistsUserReturnsFalse() {
        when(existsUserPort.existsByUsernameOrEmail("bob", "bob@mail.com")).thenReturn(false);

        boolean result = service.existsByUsernameOrEmail("bob", "bob@mail.com");

        assertThat(result).isFalse();
        verify(existsUserPort).existsByUsernameOrEmail("bob", "bob@mail.com");
    }

    @Test
    @DisplayName("should propagate exception thrown by ExistsUserPort")
    void testExistsUserPropagatesException() {
        when(existsUserPort.existsByUsernameOrEmail("charlie", "charlie@mail.com"))
                .thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> service.existsByUsernameOrEmail("charlie", "charlie@mail.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }
}
