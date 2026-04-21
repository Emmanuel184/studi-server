package com.studi.server.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import com.studi.server.model.AppUser;
import com.studi.server.support.InMemoryRepositories;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class SecurityComponentTest {

    private static final String SECRET = "change-this-test-secret-change-this-test-secret";

    @Test
    void userPrincipalExposesUserDetails() {
        AppUser user = new AppUser("emmanuel", "hash");
        ReflectionTestUtils.setField(user, "id", 9L);
        UserPrincipal principal = new UserPrincipal(user);

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

        assertThat(principal.getId()).isEqualTo(9L);
        assertThat(principal.getUsername()).isEqualTo("emmanuel");
        assertThat(principal.getPassword()).isEqualTo("hash");
        assertThat(authorities).isEmpty();
    }

    @Test
    void appUserDetailsServiceLoadsPrincipalOrThrows() {
        InMemoryRepositories.Fakes fakes = InMemoryRepositories.fakes();
        AppUser user = fakes.saveUser("emmanuel");
        AppUserDetailsService service = new AppUserDetailsService(fakes.userRepository());

        UserPrincipal principal = (UserPrincipal) service.loadUserByUsername("emmanuel");

        assertThat(principal.getId()).isEqualTo(user.getId());
        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found: missing");
    }

    @Test
    void jwtServiceGeneratesReadableValidTokens() {
        JwtService jwtService = new JwtService(SECRET, 60_000L);
        AppUser user = new AppUser("emmanuel", "hash");
        ReflectionTestUtils.setField(user, "id", 11L);
        UserPrincipal principal = new UserPrincipal(user);

        String token = jwtService.generateToken(principal);

        assertThat(jwtService.getUsername(token)).isEqualTo("emmanuel");
        assertThat(jwtService.isValid(token, principal)).isTrue();
    }

    @Test
    void jwtServiceRejectsTokensForAnotherPrincipal() {
        JwtService jwtService = new JwtService(SECRET, 60_000L);
        AppUser user = new AppUser("emmanuel", "hash");
        AppUser otherUser = new AppUser("josetta", "hash");
        ReflectionTestUtils.setField(user, "id", 11L);
        ReflectionTestUtils.setField(otherUser, "id", 12L);
        String token = jwtService.generateToken(new UserPrincipal(user));

        assertThat(jwtService.isValid(token, new UserPrincipal(otherUser))).isFalse();
    }

    @Test
    void jwtServiceRejectsMalformedAndExpirationlessTokens() {
        JwtService jwtService = new JwtService(SECRET, 60_000L);
        AppUser user = new AppUser("emmanuel", "hash");
        ReflectionTestUtils.setField(user, "id", 11L);
        UserPrincipal principal = new UserPrincipal(user);
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String expirationlessToken = Jwts.builder()
                .subject("emmanuel")
                .signWith(key)
                .compact();

        assertThat(jwtService.isValid("not-a-jwt", principal)).isFalse();
        assertThat(jwtService.isValid(expirationlessToken, principal)).isFalse();
    }
}
