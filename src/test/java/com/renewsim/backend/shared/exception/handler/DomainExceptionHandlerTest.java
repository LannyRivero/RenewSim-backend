package com.renewsim.backend.shared.exception.handler;

import com.renewsim.backend.shared.dto.ErrorResponse;
import com.renewsim.backend.shared.exception.ConflictException;
import com.renewsim.backend.shared.exception.EntityNotFoundException;
import com.renewsim.backend.shared.exception.UserAlreadyExistsException;
import com.renewsim.backend.shared.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DomainExceptionHandlerTest {

    private DomainExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new DomainExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/users/123");
    }

    @Test
    @DisplayName("should return 404 for EntityNotFoundException")
    void testHandleEntityNotFound() {
        EntityNotFoundException ex = new EntityNotFoundException("User with id 123 not found");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getErrorCode()).isEqualTo("ENTITY_NOT_FOUND");
        assertThat(response.getBody().getError()).isEqualTo("Entity not found");
        assertThat(response.getBody().getMessage()).isEqualTo("User with id 123 not found");
        assertThat(response.getBody().getPath()).isEqualTo("/api/v1/users/123");
    }

    @Test
    @DisplayName("should return 404 for UserNotFoundException")
    void testHandleUserNotFound() {
        UserNotFoundException ex = new UserNotFoundException("User alice not found");

        ResponseEntity<ErrorResponse> response = handler.handleNotFound(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getErrorCode()).isEqualTo("ENTITY_NOT_FOUND");
    }

    @Test
    @DisplayName("should return 409 for ConflictException")
    void testHandleConflict() {
        ConflictException ex = new ConflictException("Resource already exists");

        ResponseEntity<ErrorResponse> response = handler.handleConflict(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getErrorCode()).isEqualTo("RESOURCE_CONFLICT");
        assertThat(response.getBody().getError()).isEqualTo("Resource conflict");
        assertThat(response.getBody().getMessage()).isEqualTo("Resource already exists");
    }

    @Test
    @DisplayName("should return 409 for UserAlreadyExistsException")
    void testHandleUserAlreadyExists() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("User alice already exists");

        ResponseEntity<ErrorResponse> response = handler.handleConflict(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getErrorCode()).isEqualTo("RESOURCE_CONFLICT");
    }
}