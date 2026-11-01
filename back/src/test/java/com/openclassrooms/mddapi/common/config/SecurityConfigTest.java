
package com.openclassrooms.mddapi.common.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(
                mock(com.openclassrooms.mddapi.auth.security.jwt.JwtAccessDeniedHandler.class),
                mock(com.openclassrooms.mddapi.auth.security.jwt.JwtAuthenticationEntryPoint.class)
        );
    }

    @Test
    void shouldReturnBCryptPasswordEncoder() {
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
        assertThat(encoder.matches("password123", encoder.encode("password123"))).isTrue();
        assertThat(encoder.matches("wrong-password", encoder.encode("password123"))).isFalse();
    }

    @Test
    void shouldReturnAuthenticationManagerFromConfiguration() throws Exception {
        AuthenticationConfiguration configuration =
                mock(AuthenticationConfiguration.class);
        AuthenticationManager expectedManager = mock(AuthenticationManager.class);

        when(configuration.getAuthenticationManager()).thenReturn(expectedManager);

        AuthenticationManager result =
                securityConfig.authenticationManager(configuration);

        assertThat(result).isSameAs(expectedManager);
    }
}