package com.openclassrooms.mddapi.auth.controller;

import com.openclassrooms.mddapi.auth.dto.LoginRequestDto;
import com.openclassrooms.mddapi.auth.dto.RegisterRequestDto;
import com.openclassrooms.mddapi.auth.dto.UsernameAvailableDto;
import com.openclassrooms.mddapi.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    // ---------------------------------
    // POST /register
    // ---------------------------------

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        String request = """
                {
                    "email": "john.doe@test.com",
                    "password": "Password1!",
                    "username": "john",
                    "firstName": "John",
                    "lastName": "Doe"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNoContent());

        verify(authService).register(any(RegisterRequestDto.class));
    }

    @Test
    void shouldRejectRegisterWithInvalidRequest() throws Exception {
        String request = """
                {
                    "email": "invalid-email",
                    "password": "weak",
                    "username": "",
                    "firstName": "",
                    "lastName": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .register(any(RegisterRequestDto.class));
    }

    // ---------------------------------
    // POST /login
    // ---------------------------------

    @Test
    void shouldLoginSuccessfully() throws Exception {
        ResponseCookie accessTokenCookie = ResponseCookie
                .from("accessToken", "access-token-value")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie
                .from("refreshToken", "refresh-token-value")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .build();

        when(authService.authenticate(any(LoginRequestDto.class)))
                .thenReturn(Map.of(
                        "accessTokenCookie", accessTokenCookie,
                        "refreshTokenCookie", refreshTokenCookie
                ));

        String request = """
                {
                    "usernameOrEmail": "john.doe@test.com",
                    "password": "Password1!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(
                        HttpHeaders.SET_COOKIE,
                        accessTokenCookie.toString(),
                        refreshTokenCookie.toString()
                ));

        verify(authService).authenticate(any(LoginRequestDto.class));
    }

    @Test
    void shouldRejectLoginWithInvalidRequest() throws Exception {
        String request = """
                {
                    "usernameOrEmail": "",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .authenticate(any(LoginRequestDto.class));
    }

    // ---------------------------------
    // POST /refresh
    // ---------------------------------

    @Test
    void shouldRefreshAccessTokenSuccessfully() throws Exception {
        ResponseCookie accessTokenCookie = ResponseCookie
                .from("accessToken", "new-access-token")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .build();

        when(authService.refreshAccessToken(any(HttpServletRequest.class)))
                .thenReturn(accessTokenCookie);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie(
                                "refreshToken",
                                "refresh-token-value"
                        )))
                .andExpect(status().isNoContent())
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        accessTokenCookie.toString()
                ));

        verify(authService)
                .refreshAccessToken(any(HttpServletRequest.class));
    }

    // ---------------------------------
    // POST /logout
    // ---------------------------------

    @Test
    void shouldLogoutSuccessfully() throws Exception {
        ResponseCookie accessCookie = ResponseCookie
                .from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        when(authService.logout()).thenReturn(Map.of(
                "accessCookie", accessCookie,
                "refreshCookie", refreshCookie
        ));

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().value("accessToken", ""))
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().httpOnly("accessToken", true))
                .andExpect(cookie().secure("accessToken", true))
                .andExpect(cookie().path("accessToken", "/"))
                .andExpect(cookie().exists("refreshToken"))
                .andExpect(cookie().value("refreshToken", ""))
                .andExpect(cookie().maxAge("refreshToken", 0))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().secure("refreshToken", true))
                .andExpect(cookie().path("refreshToken", "/"));

        verify(authService).logout();
    }

    // ---------------------------------
    // GET /username-available
    // ---------------------------------

    @Test
    void shouldReturnUsernameAvailability() throws Exception {
        UsernameAvailableDto response = new UsernameAvailableDto(false);

        when(authService.usernameAvailable("john"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/auth/username-available")
                        .param("username", "john"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.available").value(false));

        verify(authService).usernameAvailable("john");
    }

    @Test
    void shouldReturnBadRequestWhenUsernameIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/auth/username-available"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}