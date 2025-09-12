
package com.renewsim.backend.auth_service.application.port.in;

import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthUseCaseTest {

    @Mock
    private AuthUseCase authUseCase;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginReturnsAuthResponse() {
        AuthRequestDTO request = new AuthRequestDTO();
        AuthResponseDTO expectedResponse = mock(AuthResponseDTO.class);
        when(authUseCase.login(request)).thenReturn(expectedResponse);

        AuthResponseDTO result = authUseCase.login(request);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(authUseCase).login(request);
    }
    @Test
    void testRegisterReturnsAuthResponse() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        AuthResponseDTO expectedResponse = mock(AuthResponseDTO.class);
        when(authUseCase.register(request)).thenReturn(expectedResponse);

        AuthResponseDTO result = authUseCase.register(request);

        assertNotNull(result);
        assertEquals(expectedResponse, result);
        verify(authUseCase).register(request);
    }
    

    @Test
    void testLoginWithNullRequest() {
        when(authUseCase.login(null)).thenReturn(null);

        AuthResponseDTO result = authUseCase.login(null);

        assertNull(result);
        verify(authUseCase).login(null);
    }

    @Test
    void testRegisterWithNullRequest() {
        when(authUseCase.register(null)).thenReturn(null);

        AuthResponseDTO result = authUseCase.register(null);

        assertNull(result);
        verify(authUseCase).register(null);
    }
}