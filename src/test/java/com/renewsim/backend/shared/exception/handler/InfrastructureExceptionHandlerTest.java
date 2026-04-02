package com.renewsim.backend.shared.exception.handler;

import com.renewsim.backend.shared.dto.ErrorResponse;
import com.renewsim.backend.shared.exception.AIServiceUnavailableException;
import com.renewsim.backend.shared.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InfrastructureExceptionHandlerTest {

    private InfrastructureExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new InfrastructureExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/simulate");
    }

    @Test
    @DisplayName("should return 429 for RateLimitExceededException")
    void testHandleRateLimit() {
        RateLimitExceededException ex = new RateLimitExceededException("Rate limit exceeded: 100 requests/hour");

        ResponseEntity<ErrorResponse> response = handler.handleRateLimit(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(429);
        assertThat(response.getBody().getErrorCode()).isEqualTo("RATE_LIMIT_EXCEEDED");
        assertThat(response.getBody().getError()).isEqualTo("Too many requests");
        assertThat(response.getBody().getMessage()).isEqualTo("Rate limit exceeded: 100 requests/hour");
    }

    @Test
    @DisplayName("should return 503 for AIServiceUnavailableException")
    void testHandleAIServiceUnavailable() {
        AIServiceUnavailableException ex = new AIServiceUnavailableException(
                "OpenAI service is temporarily unavailable");

        ResponseEntity<ErrorResponse> response = handler.handleAIServiceUnavailable(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(503);
        assertThat(response.getBody().getErrorCode()).isEqualTo("AI_SERVICE_UNAVAILABLE");
        assertThat(response.getBody().getError()).isEqualTo("AI service unavailable");
        assertThat(response.getBody().getMessage()).isEqualTo("OpenAI service is temporarily unavailable");
    }

    @Test
    @DisplayName("should return 500 for generic Exception")
    void testHandleGeneric() {
        Exception ex = new RuntimeException("Unexpected database error");

        ResponseEntity<ErrorResponse> response = handler.handleGeneric(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().getError()).isEqualTo("Internal server error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }
}
