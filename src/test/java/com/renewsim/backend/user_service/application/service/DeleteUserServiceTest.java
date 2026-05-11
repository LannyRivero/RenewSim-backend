package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

@DisplayName("DeleteUserService")
class DeleteUserServiceTest {

    private UserRepositoryPort userRepositoryPort;
    private DeleteUserService service;

    @BeforeEach
    void setUp() {
        userRepositoryPort = mock(UserRepositoryPort.class);
        service = new DeleteUserService(userRepositoryPort);
    }

    @Nested
    @DisplayName("Given user exists")
    class UserExists {

        @BeforeEach
        void setup() {
            when(userRepositoryPort.existsById(1L)).thenReturn(true);
        }

        @Test
        @DisplayName("should delete user successfully")
        void shouldDeleteUser() {
            assertThatNoException().isThrownBy(() -> service.deleteUser(1L));
            verify(userRepositoryPort).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("Given user does not exist")
    class UserNotFound {

        @BeforeEach
        void setup() {
            when(userRepositoryPort.existsById(99L)).thenReturn(false);
        }

        @Test
        @DisplayName("should throw UserNotFoundException")
        void shouldThrow() {
            assertThatThrownBy(() -> service.deleteUser(99L))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("should never call deleteById")
        void shouldNeverDelete() {
            assertThatThrownBy(() -> service.deleteUser(99L))
                    .isInstanceOf(UserNotFoundException.class);
            verify(userRepositoryPort, never()).deleteById(any());
        }
    }
}