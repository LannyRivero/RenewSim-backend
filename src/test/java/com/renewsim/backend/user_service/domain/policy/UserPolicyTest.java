package com.renewsim.backend.user_service.domain.policy;

import com.renewsim.backend.shared.exception.InvalidUserDataException;
import com.renewsim.backend.user_service.domain.service.UserPolicy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserPolicyTest {

    // ---------------------------
    // Username normalization
    // ---------------------------
    @Test
    @DisplayName("normalizeUsername should return null when input is null")
    void normalizeUsername_nullInput_returnsNull() {
        assertThat(UserPolicy.normalizeUsername(null)).isNull();
    }

    @Test
    @DisplayName("normalizeUsername should trim and lowercase input")
    void normalizeUsername_trimAndLowercase() {
        assertThat(UserPolicy.normalizeUsername("  Alice  ")).isEqualTo("alice");
    }

    @Test
    @DisplayName("normalizeUsername should handle already lowercase")
    void normalizeUsername_alreadyLowercase() {
        assertThat(UserPolicy.normalizeUsername("bob")).isEqualTo("bob");
    }

    @Test
    @DisplayName("normalizeUsername should handle empty string")
    void normalizeUsername_emptyString() {
        assertThat(UserPolicy.normalizeUsername("")).isEqualTo("");
    }

    @Test
    @DisplayName("normalizeUsername should trim to empty when only spaces")
    void normalizeUsername_onlySpaces() {
        assertThat(UserPolicy.normalizeUsername("   ")).isEqualTo("");
    }

    // ---------------------------
    // Email normalization
    // ---------------------------
    @Test
    @DisplayName("normalizeEmail should return null when input is null")
    void normalizeEmail_nullInput_returnsNull() {
        assertThat(UserPolicy.normalizeEmail(null)).isNull();
    }

    @Test
    @DisplayName("normalizeEmail should trim and lowercase input")
    void normalizeEmail_trimAndLowercase() {
        assertThat(UserPolicy.normalizeEmail("  Alice@Mail.COM  ")).isEqualTo("alice@mail.com");
    }

    @Test
    @DisplayName("normalizeEmail should handle already normalized email")
    void normalizeEmail_alreadyNormalized() {
        assertThat(UserPolicy.normalizeEmail("user@mail.com")).isEqualTo("user@mail.com");
    }

    @Test
    @DisplayName("normalizeEmail should handle empty string")
    void normalizeEmail_emptyString() {
        assertThat(UserPolicy.normalizeEmail("")).isEqualTo("");
    }

    @Test
    @DisplayName("normalizeEmail should trim to empty when only spaces")
    void normalizeEmail_onlySpaces() {
        assertThat(UserPolicy.normalizeEmail("   ")).isEqualTo("");
    }

    // ---------------------------
    // Password strength
    // ---------------------------
    @Test
    @DisplayName("validatePasswordStrength should accept valid password")
    void validatePasswordStrength_valid() {
        assertThatCode(() -> UserPolicy.validatePaswordStrength("StrongPass1"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validatePasswordStrength should throw when password is null")
    void validatePasswordStrength_null_throws() {
        assertThatThrownBy(() -> UserPolicy.validatePaswordStrength(null))
                .isInstanceOf(InvalidUserDataException.class)
                .hasMessageContaining("Password must contain");
    }

    @Test
    @DisplayName("validatePasswordStrength should throw when password too short")
    void validatePasswordStrength_tooShort_throws() {
        assertThatThrownBy(() -> UserPolicy.validatePaswordStrength("Ab1"))
                .isInstanceOf(InvalidUserDataException.class);
    }

    @Test
    @DisplayName("validatePasswordStrength should throw when missing uppercase")
    void validatePasswordStrength_missingUppercase_throws() {
        assertThatThrownBy(() -> UserPolicy.validatePaswordStrength("weakpassword1"))
                .isInstanceOf(InvalidUserDataException.class);
    }

    @Test
    @DisplayName("validatePasswordStrength should throw when missing number")
    void validatePasswordStrength_missingNumber_throws() {
        assertThatThrownBy(() -> UserPolicy.validatePaswordStrength("WeakPassword"))
                .isInstanceOf(InvalidUserDataException.class);
    }

    @Test
    @DisplayName("validatePasswordStrength should throw when valid pattern but too short")
    void validatePasswordStrength_shortValidPattern() {
        assertThatThrownBy(() -> UserPolicy.validatePaswordStrength("A1"))
                .isInstanceOf(InvalidUserDataException.class);
    }

    @Test
    @DisplayName("validatePasswordStrength should accept valid password with symbols")
    void validatePasswordStrength_withSymbols() {
        assertThatCode(() -> UserPolicy.validatePaswordStrength("Strong#Pass1"))
                .doesNotThrowAnyException();
    }
}

