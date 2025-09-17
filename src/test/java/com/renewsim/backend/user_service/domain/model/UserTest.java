package com.renewsim.backend.user_service.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("should default roles to empty set when null is provided")
    void testUserWithNullRoles() {
        User user = new User(1L, "john", "john@mail.com", true, null, null, null, "hash123");

        assertThat(user.roles()).isEmpty();
        assertThat(user.username()).isEqualTo("john");
    }

    @Test
    @DisplayName("should copy roles into unmodifiable set when provided")
    void testUserWithRoles() {
        Set<String> roles = Set.of("USER", "ADMIN");
        User user = new User(2L, "alice", "alice@mail.com", true, roles, null, null, "hash456");

        assertThat(user.roles()).containsExactlyInAnyOrder("USER", "ADMIN");
        assertThatThrownBy(() -> user.roles().add("NEW_ROLE"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("should handle empty roles set correctly")
    void testUserWithEmptyRoles() {
        User user = new User(3L, "bob", "bob@mail.com", true, Set.of(), null, null, "hash789");

        assertThat(user.roles()).isEmpty();
    }

    // Invariants
    @Test
    @DisplayName("should throw when username is null")
    void testUsernameNull() {
        assertThatThrownBy(() -> new User(1L, null, "mail@mail.com", true, null, null, null, "hash"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Username cannot be null");
    }

    @Test
    @DisplayName("should throw when username is blank")
    void testUsernameBlank() {
        assertThatThrownBy(() -> new User(1L, "   ", "mail@mail.com", true, null, null, null, "hash"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username must not be blank");
    }

    @Test
    @DisplayName("should throw when email is null")
    void testEmailNull() {
        assertThatThrownBy(() -> new User(1L, "john", null, true, null, null, null, "hash"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Email cannot be null");
    }

    @Test
    @DisplayName("should throw when passwordHash is null")
    void testPasswordNull() {
        assertThatThrownBy(() -> new User(1L, "john", "john@mail.com", true, null, null, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Password cannot be null");
    }

    @Test
    @DisplayName("should throw when passwordHash shorter than 6 chars")
    void testPasswordTooShort() {
        assertThatThrownBy(() -> new User(1L, "john", "john@mail.com", true, null, null, null, "123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password must be at least 6 characters long");
    }

    // Factory method tests
    @Test
    @DisplayName("factory create() should build valid user with defaults")
    void testCreateUserValid() {
        User user = User.create("john", "john@mail.com", "secure123", Set.of("USER"));

        assertThat(user.username()).isEqualTo("john");
        assertThat(user.email()).isEqualTo("john@mail.com");
        assertThat(user.enabled()).isTrue();
        assertThat(user.createdAt()).isNotNull();
        assertThat(user.updatedAt()).isNotNull();
        assertThat(user.roles()).containsExactly("USER");
    }

    @Test
    @DisplayName("factory create() should handle null roles with empty set")
    void testCreateUserWithNullRoles() {
        User user = User.create("john", "john@mail.com", "secure123", null);

        assertThat(user.roles()).isEmpty();
    }
}

