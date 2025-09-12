package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.user_service.application.port.out.SearchUserPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.dto.PageResponse;
import com.renewsim.backend.user_service.dto.UserFilterRequest;
import com.renewsim.backend.user_service.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListUsersServiceTest {

    @Mock
    private SearchUserPort searchUserPort;

    @InjectMocks
    private ListUsersService service;

    private User sampleUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        sampleUser = new User(
                1L,
                "alice",
                "alice@mail.com",
                true,
                Set.of("USER"),
                null,
                null,
                "StrongPass1");
    }

    @Test
    @DisplayName("should return paginated list of users")
    void testListUsersWithPagination() {
        UserFilterRequest filters = new UserFilterRequest(null, null, null);
        Page<User> page = new PageImpl<>(List.of(sampleUser));

        when(searchUserPort.search(null, null, null, 0, 20)).thenReturn(page);

        PageResponse<UserResponse> result = service.listUsers(0, 20, filters);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).username()).isEqualTo("alice");
        verify(searchUserPort).search(null, null, null, 0, 20);
    }

    @Test
    @DisplayName("should filter users by username")
    void testFilterByUsername() {
        UserFilterRequest filters = new UserFilterRequest("alice", null, null);
        Page<User> page = new PageImpl<>(List.of(sampleUser));

        when(searchUserPort.search("alice", null, null, 0, 20)).thenReturn(page);

        PageResponse<UserResponse> result = service.listUsers(0, 20, filters);

        assertThat(result.content()).extracting(UserResponse::username).containsExactly("alice");
    }

    @Test
    @DisplayName("should filter users by email")
    void testFilterByEmail() {
        UserFilterRequest filters = new UserFilterRequest(null, "alice@mail.com", null);
        Page<User> page = new PageImpl<>(List.of(sampleUser));

        when(searchUserPort.search(null, "alice@mail.com", null, 0, 20)).thenReturn(page);

        PageResponse<UserResponse> result = service.listUsers(0, 20, filters);

        assertThat(result.content()).extracting(UserResponse::email).containsExactly("alice@mail.com");
    }

    @Test
    @DisplayName("should filter users by enabled status")
    void testFilterByEnabledStatus() {
        UserFilterRequest filters = new UserFilterRequest(null, null, true);
        Page<User> page = new PageImpl<>(List.of(sampleUser));

        when(searchUserPort.search(null, null, true, 0, 20)).thenReturn(page);

        PageResponse<UserResponse> result = service.listUsers(0, 20, filters);

        assertThat(result.content()).extracting(UserResponse::enabled).containsExactly(true);
    }

    @Test
    @DisplayName("should return empty list when page has no users")
    void testEmptyPageReturnsEmptyList() {
        UserFilterRequest filters = new UserFilterRequest(null, null, null);
        Page<User> page = new PageImpl<>(List.of());

        when(searchUserPort.search(null, null, null, 1, 20)).thenReturn(page);

        PageResponse<UserResponse> result = service.listUsers(1, 20, filters);

        assertThat(result.content()).isEmpty();
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when page < 0")
    void testInvalidPageThrows() {
        UserFilterRequest filters = new UserFilterRequest(null, null, null);

        assertThatThrownBy(() -> service.listUsers(-1, 20, filters))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("should throw IllegalArgumentException when size = 0")
    void testInvalidSizeThrows() {
        UserFilterRequest filters = new UserFilterRequest(null, null, null);

        assertThatThrownBy(() -> service.listUsers(0, 0, filters))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
