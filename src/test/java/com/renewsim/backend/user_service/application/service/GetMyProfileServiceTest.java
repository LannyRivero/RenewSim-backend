package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.shared.domain.vo.RoleName;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMyProfileService")
class GetMyProfileServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;
    @Mock
    private UserServiceMapper mapper;

    @InjectMocks
    private GetMyProfileService service;

    private static final String HASH = new BCryptPasswordEncoder(12).encode("pass");

    private User buildUser(Long id) {
        return User.reconstitute(
                id,
                "john@example.com",
                HASH,
                "John",
                null,
                UserStatus.ACTIVE,
                Set.of(RoleName.USER),
                LocalDateTime.now(),
                LocalDateTime.now(),
                true,
                LocalDateTime.now());
    }

    private UserResponse buildResponse(Long id) {
        return new UserResponse(id, "john", "john@example.com", "John", null,
                "ACTIVE", Set.of("USER"), null, null);
    }

    @Test
    @DisplayName("existing userId -> returns UserResponse")
    void getMyProfile_existingUser_returnsResponse() {
        User user = buildUser(1L);
        UserResponse response = buildResponse(1L);

        when(userRepositoryPort.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponse result = service.getMyProfile(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("john@example.com");
    }

    @Test
    @DisplayName("unknown userId -> throws UserNotFoundException")
    void getMyProfile_unknownUser_throws() {
        when(userRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMyProfile(99L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("99");
    }
}