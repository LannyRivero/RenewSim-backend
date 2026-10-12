package com.renewsim.backend.auth_service.application.validator;

import com.renewsim.backend.auth_service.application.port.out.PasswordEncoderPort;
import com.renewsim.backend.shared.exception.AuthenticationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CredentialsValidator")
class CredentialsValidatorTest {

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Test
    @DisplayName("should accept matching password")
    void validatePassword_matchingPassword_doesNotThrow() {
        CredentialsValidator validator = new CredentialsValidator(passwordEncoder);
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);

        assertThatCode(() -> validator.validatePassword("raw", "encoded"))
                .doesNotThrowAnyException();

        verify(passwordEncoder).matches("raw", "encoded");
    }

    @Test
    @DisplayName("should throw authentication error with auth code when password does not match")
    void validatePassword_nonMatchingPassword_throwsAuthErrorMessage() {
        CredentialsValidator validator = new CredentialsValidator(passwordEncoder);
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> validator.validatePassword("raw", "encoded"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("AUTH_INVALID_CREDENTIALS: Invalid credentials");

        verify(passwordEncoder).matches("raw", "encoded");
    }
}
