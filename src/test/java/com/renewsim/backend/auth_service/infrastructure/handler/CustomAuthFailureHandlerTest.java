package com.renewsim.backend.auth_service.infrastructure.handler;

import static org.mockito.Mockito.*;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class CustomAuthFailureHandlerTest {

    private CustomAuthFailureHandler failureHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException exception;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        failureHandler = new CustomAuthFailureHandler();
    }

    @Test
    void onAuthenticationFailure_ShouldSetErrorResponseAndLogFailure() throws IOException {
        // Arrange
        String username = "testUser";
        String clientIp = "127.0.0.1";
        String errorMessage = "Invalid credentials";

        when(request.getParameter("username")).thenReturn(username);
        when(request.getRemoteAddr()).thenReturn(clientIp);
        when(exception.getMessage()).thenReturn(errorMessage);

        // Act
        failureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
        verify(request).getParameter("username");
        verify(request).getRemoteAddr();
        verify(exception).getMessage();
    }
}