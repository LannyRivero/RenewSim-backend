package com.renewsim.backend.shared.domain.vo;

import org.junit.jupiter.api.Test;

import com.renewsim.backend.shared.domain.exception.InvalidEmailException;

import static org.assertj.core.api.Assertions.*;

class EmailTest {
    
    @Test
    void shouldCreateValidEmail() {
        Email email = new Email("user@example.com");
        assertThat(email.value()).isEqualTo("user@example.com");
    }
    
    @Test
    void shouldNormalizeToLowerCase() {
        Email email = new Email("USER@EXAMPLE.COM");
        assertThat(email.value()).isEqualTo("user@example.com");
    }
    
    @Test
    void shouldTrimWhitespace() {
        Email email = new Email("  user@example.com  ");
        assertThat(email.value()).isEqualTo("user@example.com");
    }
    
    @Test
    void shouldRejectNullEmail() {
        assertThatThrownBy(() -> new Email(null))
            .isInstanceOf(InvalidEmailException.class)
            .hasMessageContaining("cannot be null");
    }
    
    @Test
    void shouldRejectBlankEmail() {
        assertThatThrownBy(() -> new Email("   "))
            .isInstanceOf(InvalidEmailException.class)
            .hasMessageContaining("cannot be null or blank");
    }
    
    @Test
    void shouldRejectInvalidFormat() {
        assertThatThrownBy(() -> new Email("invalid"))
            .isInstanceOf(InvalidEmailException.class)
            .hasMessageContaining("Invalid email format");
    }
    
    @Test
    void shouldRejectMissingAt() {
        assertThatThrownBy(() -> new Email("userexample.com"))
            .isInstanceOf(InvalidEmailException.class);
    }
    
    @Test
    void shouldRejectMissingDomain() {
        assertThatThrownBy(() -> new Email("user@"))
            .isInstanceOf(InvalidEmailException.class);
    }
}