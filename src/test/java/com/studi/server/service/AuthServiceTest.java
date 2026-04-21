package com.studi.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.studi.server.dto.AuthRequest;
import com.studi.server.dto.AuthResponse;
import com.studi.server.model.AppUser;
import com.studi.server.security.JwtService;
import com.studi.server.security.UserPrincipal;
import com.studi.server.support.InMemoryRepositories;

class AuthServiceTest {

    @Test
    void registerNormalizesUsernameEncodesPasswordAndReturnsToken() {
        InMemoryRepositories.Fakes fakes = InMemoryRepositories.fakes();
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationProvider authenticationProvider = mock(AuthenticationProvider.class);
        JwtService jwtService = mock(JwtService.class);
        AuthService service = new AuthService(
                fakes.userRepository(),
                passwordEncoder,
                authenticationProvider,
                jwtService);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("jwt-token");

        AuthResponse response = service.register(authRequest("  Emmanuel  ", "secret"));

        AppUser savedUser = fakes.userRepository().findByUsername("emmanuel").orElseThrow();
        assertThat(savedUser.getPasswordHash()).isEqualTo("encoded-secret");
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(savedUser.getId());
        assertThat(response.getUsername()).isEqualTo("emmanuel");
    }

    @Test
    void registerRejectsDuplicateAndMissingUsernames() {
        InMemoryRepositories.Fakes fakes = InMemoryRepositories.fakes();
        fakes.saveUser("taken");
        AuthService service = new AuthService(
                fakes.userRepository(),
                mock(PasswordEncoder.class),
                mock(AuthenticationProvider.class),
                mock(JwtService.class));

        assertThatThrownBy(() -> service.register(authRequest(" TAKEN ", "secret")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists");
        assertThatThrownBy(() -> service.register(authRequest(null, "secret")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username is required");
        assertThatThrownBy(() -> service.login(authRequest("   ", "secret")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username is required");
    }

    @Test
    void loginAuthenticatesNormalizedUsernameAndReturnsToken() {
        InMemoryRepositories.Fakes fakes = InMemoryRepositories.fakes();
        AppUser user = fakes.saveUser("emmanuel");
        AuthenticationProvider authenticationProvider = mock(AuthenticationProvider.class);
        JwtService jwtService = mock(JwtService.class);
        AuthService service = new AuthService(
                fakes.userRepository(),
                mock(PasswordEncoder.class),
                authenticationProvider,
                jwtService);
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("jwt-token");

        AuthResponse response = service.login(authRequest(" EMMANUEL ", "secret"));

        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationProvider).authenticate(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getName()).isEqualTo("emmanuel");
        assertThat(tokenCaptor.getValue().getCredentials()).isEqualTo("secret");
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(user.getId());
        assertThat(response.getUsername()).isEqualTo("emmanuel");
    }

    @Test
    void loginRejectsUnknownUserAfterAuthenticationAttempt() {
        InMemoryRepositories.Fakes fakes = InMemoryRepositories.fakes();
        AuthenticationProvider authenticationProvider = mock(AuthenticationProvider.class);
        AuthService service = new AuthService(
                fakes.userRepository(),
                mock(PasswordEncoder.class),
                authenticationProvider,
                mock(JwtService.class));

        assertThatThrownBy(() -> service.login(authRequest("missing", "secret")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid username or password");
        verify(authenticationProvider).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    private AuthRequest authRequest(String username, String password) {
        AuthRequest request = new AuthRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}
