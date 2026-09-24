package com.openclassrooms.mddapi.auth.controller;

import com.openclassrooms.mddapi.auth.dto.LoginRequestDto;
import com.openclassrooms.mddapi.auth.dto.RegisterRequestDto;
import com.openclassrooms.mddapi.auth.dto.UsernameAvailableDto;
import com.openclassrooms.mddapi.auth.service.AuthService;
import com.openclassrooms.mddapi.factory.MediaTestFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.contains;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void shouldRegisterSuccessfully() throws Exception {
        MockMultipartFile file = MediaTestFactory.createMedia();
        doNothing().when(authService)
                .register(any(RegisterRequestDto.class), file);

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "email": "john.doe@test.com",
                                            "password": "Password1!",
                                            "username": "john",
                                            "firstName": "John",
                                            "lastName": "Doe"
                                        }
                                        """)
                )
                .andExpect(status().isNoContent());

        verify(authService)
                .register(any(RegisterRequestDto.class), file);

    }

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

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .cookie(
                                        new Cookie(
                                                "refreshToken",
                                                "refresh-token-value"
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.SET_COOKIE,
                        accessTokenCookie.toString()
                ));

        verify(authService)
                .refreshAccessToken(any(HttpServletRequest.class));

    }

    @Test
    void shouldLogoutSuccessfully() throws Exception {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "").httpOnly(true).secure(true).path("/").maxAge(0).build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "").httpOnly(true).secure(true).path("/").maxAge(0).build();

        Map<String, ResponseCookie> cookies = Map.of("accessCookie", accessCookie, "refreshCookie", refreshCookie);

        when(authService.logout()).thenReturn(cookies);

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().stringValues(
                        HttpHeaders.SET_COOKIE,
                        contains(
                                containsString("accessToken="),
                                containsString("refreshToken=")
                        )));

        verify(authService).logout();
    }

    @Test
    void shouldReturnUsernameAvailable() throws Exception {
        UsernameAvailableDto response =
                new UsernameAvailableDto(false);

        when(authService.usernameAvailable("john"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/auth/username-available/john"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(jsonPath("$.exist")
                        .value(false));

        verify(authService)
                .usernameAvailable("john");

    }

    @Test
    void shouldRejectLoginWithInvalidRequest() throws Exception {
        String loginRequestContent = """
                    { 
                        "email": "", 
                        "password": "" 
                    }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequestContent)
                )
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .authenticate(any(LoginRequestDto.class));

    }

    @Test
    void shouldRejectRegisterWithInvalidRequest() throws Exception {
        MockMultipartFile file = MediaTestFactory.createMedia();

        String registerRequestContent = """
                    {
                        "email": "invalid-email",
                        "password": "weak",
                        "username": "",
                        "firstName": "",
                        "lastName": ""
                    }
                """;

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerRequestContent)
                )
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .register(any(RegisterRequestDto.class), file);

    }

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

        Map<String, ResponseCookie> cookies = Map.of(
                "accessTokenCookie", accessTokenCookie,
                "refreshTokenCookie", refreshTokenCookie
        );

        when(authService.authenticate(any(LoginRequestDto.class)))
                .thenReturn(cookies);

        String loginRequestContent = """
                    {
                        "email": "john.doe@test.com",
                        "password": "Password1!"
                    }
                """;

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequestContent)
                )
                .andExpect(status().isOk())
                .andExpect(header().stringValues(
                        HttpHeaders.SET_COOKIE,
                        accessTokenCookie.toString(),
                        refreshTokenCookie.toString()
                ));

        verify(authService)
                .authenticate(any(LoginRequestDto.class));

    }
}
