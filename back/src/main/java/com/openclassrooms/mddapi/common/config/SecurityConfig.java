package com.openclassrooms.mddapi.common.config;

import com.openclassrooms.mddapi.auth.security.cookie.CookieBearerTokenResolver;
import com.openclassrooms.mddapi.auth.security.jwt.JwtAccessDeniedHandler;
import com.openclassrooms.mddapi.auth.security.jwt.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * Creates the password encoder used to securely hash user passwords.
     *
     * <p>BCrypt is used to generate one-way password hashes. Passwords should
     * never be stored or compared in plain text.</p>
     *
     * @return the BCrypt password encoder used by Spring Security
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the application's Spring Security filter chain.
     *
     * <p>The application uses stateless authentication with JWT tokens. CSRF
     * protection is disabled because authentication is handled through the
     * configured JWT-based security mechanism.</p>
     *
     * <p>Authentication is required for user, article and comment endpoints,
     * while authentication endpoints are publicly accessible.</p>
     *
     * <p>JWT authentication is performed by the OAuth 2.0 resource server using
     * the configured {@link CookieBearerTokenResolver} to retrieve the bearer
     * token from the HTTP cookie.</p>
     *
     * @param http the HTTP security configuration
     * @param cookieBearerTokenResolver resolver used to extract the JWT from the request
     * @return the configured Spring Security filter chain
     * @throws Exception if the security configuration cannot be built
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CookieBearerTokenResolver cookieBearerTokenResolver
    ) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/users/**").authenticated()
                        .requestMatchers("/api/v1/articles/**").authenticated()
                        .requestMatchers("/api/v1/comments/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(cookieBearerTokenResolver)
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                );

        return http.build();
    }

    /**
     * Exposes the Spring Security {@link AuthenticationManager} used to
     * authenticate user credentials.
     *
     * <p>The authentication manager delegates authentication to the configured
     * authentication providers, including the application's
     * {@code UserDetailsService} and {@link PasswordEncoder}.</p>
     *
     * @param configuration Spring Security authentication configuration
     * @return the configured authentication manager
     * @throws Exception if the authentication manager cannot be created
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
