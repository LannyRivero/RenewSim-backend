package com.renewsim.backend.shared.domain.vo;

import com.renewsim.backend.shared.domain.exception.InvalidPasswordException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PasswordTest {
    
    @Test
    void shouldCreateValidPassword() {
        Password password = new Password("SecureP@ss123");
        assertThat(password.value()).isEqualTo("SecureP@ss123");
    }
    
    @Test
    void shouldRejectNull() {
        assertThatThrownBy(() -> new Password(null))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("cannot be null");
    }
    
    @Test
    void shouldRejectTooShort() {
        assertThatThrownBy(() -> new Password("Short1!"))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("at least 8 characters");
    }
    
    @Test
    void shouldRejectMissingUppercase() {
        assertThatThrownBy(() -> new Password("password123!"))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("uppercase letter");
    }
    
    @Test
    void shouldRejectMissingDigit() {
        assertThatThrownBy(() -> new Password("Password!"))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("digit");
    }
    
    @Test
    void shouldRejectMissingSymbol() {
        assertThatThrownBy(() -> new Password("Password123"))
            .isInstanceOf(InvalidPasswordException.class)
            .hasMessageContaining("symbol");
    }
    
    @Test
    void shouldAcceptValidPasswordWithAllRequirements() {
        assertThatCode(() -> new Password("ValidP@ssw0rd"))
            .doesNotThrowAnyException();
    }
}