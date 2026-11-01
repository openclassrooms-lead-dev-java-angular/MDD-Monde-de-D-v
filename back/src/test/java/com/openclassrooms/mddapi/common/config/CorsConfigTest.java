package com.openclassrooms.mddapi.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    private final CorsConfig corsConfig = new CorsConfig();

    @Test
    void shouldConfigureCorsCorrectly() {
        CorsConfigurationSource source =
                corsConfig.corsConfigurationSource();

        CorsConfiguration configuration =
                source.getCorsConfiguration(
                        new org.springframework.mock.web.MockHttpServletRequest(
                                "GET", "/api/v1/articles"
                        )
                );

        assertThat(configuration).isNotNull();
        assertThat(configuration.getAllowedOrigins())
                .containsExactly("http://localhost:4200");

        assertThat(configuration.getAllowedMethods())
                .containsExactly("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");

        assertThat(configuration.getAllowedHeaders())
                .containsExactly("*");

        assertThat(configuration.getAllowCredentials()).isTrue();
    }

    @Test
    void shouldApplyCorsConfigurationToAllPaths() {
        CorsConfigurationSource source =
                corsConfig.corsConfigurationSource();

        CorsConfiguration configuration =
                source.getCorsConfiguration(
                        new org.springframework.mock.web.MockHttpServletRequest(
                                "GET", "/another/path"
                        )
                );

        assertThat(configuration).isNotNull();
    }
}
