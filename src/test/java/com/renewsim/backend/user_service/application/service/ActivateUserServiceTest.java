package com.renewsim.backend.user_service.application.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.ResourceNotFoundException;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.domain.model.UserStatus;

@ExtendWith(MockitoExtension.class)
class ActivateUserServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private ActivateUserService activateUserService;

    @Test
    @DisplayName("Should activate inactive user successfully")
    void activate_whenUserIsInactive_shouldActivateSuccessfully() {
        User user = User.reconstitute(
                1L,
                "test@example.com",
                "$2a$10$fy0.LwGEzGOCk0Qjs6f5We2P0RnL7RfoYm6thyjPZRQSuv9L3qV.S",
                "Test User",
                "123456789",
                UserStatus.INACTIVE,
                Set.of(RoleName.USER),
                LocalDateTime.now().minusDays(1),
                null
        );

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));

        activateUserService.activate(1L);

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getActivatedAt());
        verify(userRepositoryPort).save(user);
    }

    @Test
    @DisplayName("Should throw when user does not exist")
    void activate_whenUserDoesNotExist_shouldThrowException() {
        when(userRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> activateUserService.activate(99L));
    }

    @Test
    @DisplayName("Should do nothing harmful when user is already active")
    void activate_whenUserAlreadyActive_shouldNotFail() {
        User user = User.reconstitute(
                1L,
                "test@example.com",
                "$2a$10$fy0.LwGEzGOCk0Qjs6f5We2P0RnL7RfoYm6thyjPZRQSuv9L3qV.S",
                "Test User",
                "123456789",
                UserStatus.ACTIVE,
                Set.of(RoleName.USER),
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1)
        );

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> activateUserService.activate(1L));
        verify(userRepositoryPort).save(user);
    }

    @Test
    @DisplayName("Should throw when user is suspended")
    void activate_whenUserIsSuspended_shouldThrowIllegalStateException() {
        User user = User.reconstitute(
                1L,
                "test@example.com",
                "$2a$10$fy0.LwGEzGOCk0Qjs6f5We2P0RnL7RfoYm6thyjPZRQSuv9L3qV.S",
                "Test User",
                "123456789",
                UserStatus.SUSPENDED,
                Set.of(RoleName.USER),
                LocalDateTime.now().minusDays(5),
                null
        );

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> activateUserService.activate(1L));
    }
}