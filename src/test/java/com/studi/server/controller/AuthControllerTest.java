package com.studi.server.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.studi.server.dto.AuthRequest;
import com.studi.server.dto.AuthResponse;
import com.studi.server.service.AuthService;

class AuthControllerTest {

    @Test
    void registerAndLoginDelegateToService() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        AuthRequest request = new AuthRequest();
        AuthResponse registerResponse = new AuthResponse("register-token", 1L, "emmanuel");
        AuthResponse loginResponse = new AuthResponse("login-token", 1L, "emmanuel");
        when(authService.register(request)).thenReturn(registerResponse);
        when(authService.login(request)).thenReturn(loginResponse);

        assertThat(controller.register(request).getBody()).isSameAs(registerResponse);
        assertThat(controller.login(request).getBody()).isSameAs(loginResponse);
    }
}
