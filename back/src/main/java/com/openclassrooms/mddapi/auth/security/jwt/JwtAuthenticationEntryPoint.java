package com.openclassrooms.mddapi.auth.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.auth.dto.JwtResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * Handles authentication failures for unauthenticated requests.
     *
     * <p>This handler is invoked when a request requires authentication but the
     * client has not provided valid authentication credentials, for example when
     * the JWT is missing, invalid or expired.</p>
     *
     * <p>The response is returned with HTTP status {@code 401 Unauthorized}
     * and a JSON body describing the authentication failure.</p>
     *
     * @param request the HTTP request that requires authentication
     * @param response the HTTP response used to return the error
     * @param authException the authentication exception that caused the failure
     * @throws IOException if an I/O error occurs while writing the response
     * @throws ServletException if the request cannot be handled
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        JwtResponseDto error = new JwtResponseDto(
                HttpStatus.UNAUTHORIZED,
                "Forbidden",
                "You do not have permission to access this resource"
        );

        response.getWriter().write(objectMapper.writeValueAsString(error));

        response.getWriter().write("""
            {
                "status": 401,
                "error": "Unauthorized",
                "message": "Authentication required"
            }
            """);
    }
}
