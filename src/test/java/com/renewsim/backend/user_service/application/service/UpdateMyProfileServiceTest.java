package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.user_service.application.command.UpdateMyProfileCommand;
import com.renewsim.backend.user_service.application.mapper.UserServiceMapper;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.domain.model.UserStatus;
import com.renewsim.backend.user_service.web.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateMyProfileService")
class UpdateMyProfileServiceTest {

    @Mock private UserRepositoryPort userRepositoryPort;
    @Mock private UserServiceMapper mapper;

    @InjectMocks private UpdateMyProfileService service;

    private static final String HASH = new BCryptPasswordEncoder(12).encode("pass");

    private User buildUser(Long id) {
        return User.reconstitute(id, "john@example.com", HASH, "John", null,
                UserStatus.ACTIVE, Set.of(RoleName.USER), LocalDateTime.now(), LocalDateTime.now());
    }

    private UserResponse buildResponse(Long id, String fullName, String phone) {
        return new UserResponse(id, "john", "john@example.com", fullName, phone,
                "ACTIVE", Set.of("USER"), null, null);
    }

    @Test
    @DisplayName("valid command -> updates profile and returns response")
    void updateMyProfile_valid_returnsUpdatedResponse() {
        User user = buildUser(1L);
        UserResponse response = buildResponse(1L, "John Updated", "+34600000000");

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));
        when(userRepositoryPort.save(any(User.class))).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponse result = service.updateMyProfile(
                new UpdateMyProfileCommand(1L, "John Updated", "+34600000000"));

        assertThat(result.fullName()).isEqualTo("John Updated");
        assertThat(result.phone()).isEqualTo("+34600000000");
        verify(userRepositoryPort).save(user);
    }

    @Test
    @DisplayName("unknown userId -> throws UserNotFoundException")
    void updateMyProfile_unknownUser_throws() {
        when(userRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateMyProfile(
                new UpdateMyProfileCommand(99L, "Name", null)))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");

        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("null fullName and phone -> accepted (optional fields)")
    void updateMyProfile_nullFields_accepted() {
        User user = buildUser(1L);
        UserResponse response = buildResponse(1L, null, null);

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));
        when(userRepositoryPort.save(any(User.class))).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponse result = service.updateMyProfile(
                new UpdateMyProfileCommand(1L, null, null));

        assertThat(result).isNotNull();
        verify(userRepositoryPort).save(user);
    }
}