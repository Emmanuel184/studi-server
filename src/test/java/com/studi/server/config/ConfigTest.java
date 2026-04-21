package com.studi.server.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import com.studi.server.model.AppUser;
import com.studi.server.security.AppUserDetailsService;
import com.studi.server.support.InMemoryRepositories;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

class ConfigTest {

    @Test
    void corsConfigRegistersAllowedOriginsMethodsAndHeaders() {
        CorsConfig config = new CorsConfig(new String[] { "http://localhost:*", "https://studi.example" });
        ExposedCorsRegistry registry = new ExposedCorsRegistry();

        config.addCorsMappings(registry);

        CorsConfiguration cors = registry.configurations().get("/**");
        assertThat(cors.getAllowedOriginPatterns()).containsExactly("http://localhost:*", "https://studi.example");
        assertThat(cors.getAllowedMethods()).containsExactly("GET", "POST", "PUT", "DELETE", "OPTIONS");
        assertThat(cors.getAllowedHeaders()).containsExactly("*");
    }

    @Test
    void dataLoaderCreatesMissingDefaultUsersAndLeavesExistingUsersAlone() throws Exception {
        InMemoryRepositories.Fakes fakes = InMemoryRepositories.fakes();
        fakes.saveUser("emmanuel");
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        DataLoaderRunner runner = new DataLoaderRunner(fakes.userRepository(), passwordEncoder);

        runner.run(null);

        assertThat(fakes.userRepository().findByUsername("emmanuel")).isPresent();
        assertThat(fakes.userRepository().findByUsername("josetta"))
                .get()
                .extracting(AppUser::getPasswordHash)
                .isEqualTo("encoded-password");
    }

    @Test
    void openApiConfigRegistersBearerSecurityScheme() {
        OpenAPI openAPI = new OpenApiConfig().studiOpenApi();

        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get("bearerAuth");
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Studi API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("0.0.1");
        assertThat(openAPI.getInfo().getDescription()).contains("Studi auth");
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
    }

    @Test
    void securityConfigCreatesWorkingPasswordEncoderAndAuthenticationProvider() {
        SecurityConfig config = new SecurityConfig();
        InMemoryRepositories.Fakes fakes = InMemoryRepositories.fakes();
        PasswordEncoder passwordEncoder = config.passwordEncoder();
        fakes.userRepository().save(new AppUser("emmanuel", passwordEncoder.encode("secret")));
        AuthenticationProvider provider = config.authenticationProvider(
                new AppUserDetailsService(fakes.userRepository()),
                passwordEncoder);

        Authentication authentication = provider.authenticate(
                new UsernamePasswordAuthenticationToken("emmanuel", "secret"));

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getName()).isEqualTo("emmanuel");
    }

    private static final class ExposedCorsRegistry extends CorsRegistry {
        Map<String, CorsConfiguration> configurations() {
            return getCorsConfigurations();
        }
    }
}
