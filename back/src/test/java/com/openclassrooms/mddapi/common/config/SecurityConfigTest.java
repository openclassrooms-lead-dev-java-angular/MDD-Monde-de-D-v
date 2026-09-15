package com.openclassrooms.mddapi.common.config;

import com.openclassrooms.mddapi.auth.security.jwt.JwtAccessDeniedHandler;
import com.openclassrooms.mddapi.auth.security.jwt.JwtAuthenticationEntryPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;
    @Mock
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @Mock
    private AuthenticationConfiguration authenticationConfiguration;
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(jwtAccessDeniedHandler, jwtAuthenticationEntryPoint);
    }

    // -------------------------------------------------------------------------
    // Password encoder
    // -------------------------------------------------------------------------
    @Test
    void shouldExposeBCryptPasswordEncoder() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    void shouldEncodePasswordUsingBCrypt() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();

        String rawPassword = "mySecurePassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        assertThat(encodedPassword)
                .isNotBlank().startsWith("$2");
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @Test
    void shouldRejectIncorrectPassword() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        String encodedPassword = passwordEncoder.encode("mySecurePassword");

        assertThat(passwordEncoder.matches("wrongPassword", encodedPassword)).isFalse();
    }

    // -------------------------------------------------------------------------
    // Authentication manager
    // -------------------------------------------------------------------------

    @Test
    void shouldExposeAuthenticationManager() throws Exception {
        AuthenticationManager authenticationManager = org.mockito.Mockito.mock(AuthenticationManager.class);

        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);
        AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);

        assertThat(result).isSameAs(authenticationManager);

        verify(authenticationConfiguration).getAuthenticationManager();
    }
}