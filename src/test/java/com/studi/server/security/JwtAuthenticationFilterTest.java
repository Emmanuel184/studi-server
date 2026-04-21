package com.studi.server.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.studi.server.model.AppUser;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final AppUserDetailsService userDetailsService = mock(AppUserDetailsService.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void skipsRequestsWithoutBearerToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain(chainCalled));

        assertThat(chainCalled).isTrue();
        verify(jwtService, never()).getUsername(null);
    }

    @Test
    void skipsAuthorizationHeadersThatAreNotBearerTokens() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc");
        AtomicBoolean chainCalled = new AtomicBoolean(false);

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain(chainCalled));

        assertThat(chainCalled).isTrue();
        verify(jwtService, never()).getUsername("abc");
    }

    @Test
    void storesAuthenticationForValidBearerToken() throws Exception {
        MockHttpServletRequest request = bearerRequest("valid-token");
        UserPrincipal principal = principal("emmanuel", 1L);
        when(jwtService.getUsername("valid-token")).thenReturn("emmanuel");
        when(userDetailsService.loadUserByUsername("emmanuel")).thenReturn(principal);
        when(jwtService.isValid("valid-token", principal)).thenReturn(true);

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain(new AtomicBoolean(false)));

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isSameAs(principal);
    }

    @Test
    void leavesContextEmptyWhenTokenHasNoUsername() throws Exception {
        MockHttpServletRequest request = bearerRequest("nameless-token");
        when(jwtService.getUsername("nameless-token")).thenReturn(null);

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain(new AtomicBoolean(false)));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userDetailsService, never()).loadUserByUsername(null);
    }

    @Test
    void leavesExistingAuthenticationUntouched() throws Exception {
        MockHttpServletRequest request = bearerRequest("valid-token");
        UsernamePasswordAuthenticationToken existing = new UsernamePasswordAuthenticationToken("current", null);
        SecurityContextHolder.getContext().setAuthentication(existing);
        when(jwtService.getUsername("valid-token")).thenReturn("emmanuel");

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain(new AtomicBoolean(false)));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existing);
        verify(userDetailsService, never()).loadUserByUsername("emmanuel");
    }

    @Test
    void leavesContextEmptyWhenTokenIsNotValidForPrincipal() throws Exception {
        MockHttpServletRequest request = bearerRequest("invalid-token");
        UserPrincipal principal = principal("emmanuel", 1L);
        when(jwtService.getUsername("invalid-token")).thenReturn("emmanuel");
        when(userDetailsService.loadUserByUsername("emmanuel")).thenReturn(principal);
        when(jwtService.isValid("invalid-token", principal)).thenReturn(false);

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain(new AtomicBoolean(false)));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void clearsContextWhenTokenHandlingFails() throws Exception {
        MockHttpServletRequest request = bearerRequest("bad-token");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("current", null));
        when(jwtService.getUsername("bad-token")).thenThrow(new RuntimeException("bad token"));

        filter.doFilterInternal(request, new MockHttpServletResponse(), chain(new AtomicBoolean(false)));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private MockHttpServletRequest bearerRequest(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private FilterChain chain(AtomicBoolean chainCalled) {
        return (request, response) -> chainCalled.set(true);
    }

    private UserPrincipal principal(String username, long id) {
        AppUser user = new AppUser(username, "hash");
        ReflectionTestUtils.setField(user, "id", id);
        return new UserPrincipal(user);
    }
}
