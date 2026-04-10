package com.renewsim.backend.user_service.domain.model;

import com.renewsim.backend.shared.domain.vo.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User Aggregate Root")
class UserTest {

    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_BCRYPT = new BCryptPasswordEncoder(12).encode("password123");

    @Test
    @DisplayName("should create inactive user with valid data")
    void create_withValidData_shouldCreateInactiveUser() {
        Set<RoleName> roles = new HashSet<>();
        roles.add(RoleName.USER);

        User user = User.create(VALID_EMAIL, VALID_BCRYPT, "John Doe", "+34600000000", roles);

        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.getActivatedAt()).isNull();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.hasRole(RoleName.USER)).isTrue();
    }

    @Test
    @DisplayName("should throw exception when email is null")
    void create_withNullEmail_shouldThrowException() {
        assertThatThrownBy(() -> User.create(null, VALID_BCRYPT, "John", null, new HashSet<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email cannot be null");
    }

    @Test
    @DisplayName("should throw exception when email format is invalid")
    void create_withInvalidEmailFormat_shouldThrowException() {
        assertThatThrownBy(() -> User.create("invalid-email", VALID_BCRYPT, "John", null, new HashSet<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email format is invalid");
    }

    @Test
    @DisplayName("should throw exception when password hash is null")
    void create_withNullPasswordHash_shouldThrowException() {
        assertThatThrownBy(() -> User.create(VALID_EMAIL, null, "John", null, new HashSet<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PasswordHash cannot be null");
    }

    @Test
    @DisplayName("should throw exception when password is plaintext instead of BCrypt hash")
    void create_withPlaintextPassword_shouldThrowException() {
        assertThatThrownBy(() -> User.create(VALID_EMAIL, "plaintext123", "John", null, new HashSet<>()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be a valid BCrypt hash");
    }

    @Test
    @DisplayName("should activate user and set activation timestamp")
    void activate_shouldChangeStatusToActiveAndSetTimestamp() {
        User user = User.create(VALID_EMAIL, VALID_BCRYPT, "John", null, new HashSet<>());

        user.activate();

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getActivatedAt()).isNotNull();
    }

    @Test
    @DisplayName("should be idempotent when activating already active user")
    void activate_whenAlreadyActive_shouldBeIdempotent() {
        User user = User.create(VALID_EMAIL, VALID_BCRYPT, "John", null, new HashSet<>());
        user.activate();
        LocalDateTime firstActivation = user.getActivatedAt();

        user.activate();

        assertThat(user.getActivatedAt()).isEqualTo(firstActivation);
    }

    @Test
    @DisplayName("should suspend active user")
    void suspend_shouldChangeStatusToSuspended() {
        User user = User.create(VALID_EMAIL, VALID_BCRYPT, "John", null, new HashSet<>());
        user.activate();

        user.suspend();

        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
    }

    @Test
    @DisplayName("should add role to user")
    void addRole_shouldAddRoleToUser() {
        User user = User.create(VALID_EMAIL, VALID_BCRYPT, "John", null, new HashSet<>());

        user.addRole(RoleName.ADMIN);

        assertThat(user.hasRole(RoleName.ADMIN)).isTrue();
    }

    @Test
    @DisplayName("should not duplicate role when adding same role twice")
    void addRole_withDuplicateRole_shouldNotDuplicate() {
        Set<RoleName> roles = new HashSet<>();
        roles.add(RoleName.USER);
        User user = User.create(VALID_EMAIL, VALID_BCRYPT, "John", null, roles);

        user.addRole(RoleName.USER);

        assertThat(user.getRoles()).hasSize(1);
    }

    @Test
    @DisplayName("should remove role from user")
    void removeRole_shouldRemoveRoleFromUser() {
        Set<RoleName> roles = new HashSet<>();
        roles.add(RoleName.USER);
        User user = User.create(VALID_EMAIL, VALID_BCRYPT, "John", null, roles);

        user.removeRole(RoleName.USER);

        assertThat(user.hasRole(RoleName.USER)).isFalse();
    }

    @Test
    @DisplayName("should return immutable copy of roles to prevent external modification")
    void getRoles_shouldReturnImmutableCopy() {
        User user = User.create(VALID_EMAIL, VALID_BCRYPT, "John", null, new HashSet<>());

        assertThatThrownBy(() -> user.getRoles().add(RoleName.ADMIN))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}