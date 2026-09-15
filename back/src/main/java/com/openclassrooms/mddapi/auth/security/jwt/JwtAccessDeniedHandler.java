package com.openclassrooms.mddapi.auth.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.mddapi.auth.dto.JwtResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * Handles authorization failures for authenticated users.
     *
     * <p>This handler is invoked when an authenticated request is rejected
     * because the current user does not have sufficient permissions to access
     * the requested resource.</p>
     *
     * <p>The response is returned with HTTP status {@code 403 Forbidden}
     * and a JSON body describing the authorization failure.</p>
     *
     * @param request the HTTP request that was denied
     * @param response the HTTP response used to return the error
     * @param accessDeniedException the exception raised when access is denied
     * @throws IOException if an I/O error occurs while writing the response
     * @throws ServletException if the request cannot be handled
     */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        JwtResponseDto error = new JwtResponseDto(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "You do not have permission to access this resource"
        );

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
