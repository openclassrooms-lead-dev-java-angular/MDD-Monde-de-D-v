package com.openclassrooms.mddapi.auth.security.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtSecurityHandlerTest {

    private ObjectMapper objectMapper;
    private JwtAccessDeniedHandler accessDeniedHandler;
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        accessDeniedHandler = new JwtAccessDeniedHandler(objectMapper);
        authenticationEntryPoint = new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Test
    void shouldReturnForbiddenResponseWhenAccessIsDenied() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        accessDeniedHandler.handle(request, response, exception);

        // Then
        int status = response.getStatus();

        assertThat(status)
                .isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentType())
                .isEqualTo("application/json");
        JsonNode json = objectMapper.readTree( response.getContentAsString() );
        assertThat(json.get("status").asText())
                .isEqualTo(HttpStatus.FORBIDDEN.name());
        assertThat(json.get("error").asText())
                .isEqualTo("Forbidden");
        assertThat(json.get("message").asText())
                .isEqualTo("You do not have permission to access this resource");
    }

    @Test
    void shouldReturnUnauthorizedResponseWhenAuthenticationFails() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = new AuthenticationException("Authentication failed") { };

        // When
        authenticationEntryPoint.commence( request, response, exception );

        // Then
        assertThat(response.getStatus())
                .isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentType())
                .isEqualTo("application/json");

        JsonNode json = objectMapper.readTree( response.getContentAsString() );

        assertThat(json.get("status").asText())
                .isEqualTo(HttpStatus.UNAUTHORIZED.name());
        assertThat(json.get("error").asText())
                .isEqualTo("Unauthorized");
        assertThat(json.get("message").asText())
                .isEqualTo("Authentication required");
    }
}
