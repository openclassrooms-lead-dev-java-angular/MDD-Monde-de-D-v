package com.openclassrooms.mddapi.auth.service;

import com.openclassrooms.mddapi.auth.dto.LoginRequestDto;
import com.openclassrooms.mddapi.auth.dto.RegisterRequestDto;
import com.openclassrooms.mddapi.auth.dto.UsernameAvailableDto;
import com.openclassrooms.mddapi.auth.exception.UnauthorizedException;
import com.openclassrooms.mddapi.auth.security.cookie.CookieService;
import com.openclassrooms.mddapi.auth.security.jwt.JwtTokenService;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsImpl;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.auth.exception.InvalidTokenException;
import com.openclassrooms.mddapi.factory.MediaTestFactory;
import com.openclassrooms.mddapi.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserDetailsImpl userDetails;

    @Mock
    private UserService userService;

    @Mock
    private CookieService cookieService;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest request;

    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                authenticationManager,
                jwtTokenService,
                userDetailsServiceImpl,
                userService,
                cookieService
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateSuccessfully() {
        LoginRequestDto loginRequest =
                new LoginRequestDto(
                        "john.doe@test.com",
                        "Password1!"
                );

        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        ResponseCookie accessCookie =
                ResponseCookie
                        .from("accessToken", accessToken)
                        .build();

        ResponseCookie refreshCookie =
                ResponseCookie
                        .from("refreshToken", refreshToken)
                        .build();

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        when(jwtTokenService.generateAccessToken(userDetails))
                .thenReturn(accessToken);

        when(jwtTokenService.generateRefreshToken(userDetails))
                .thenReturn(refreshToken);

        when(cookieService.generateAccessTokenCookie(accessToken))
                .thenReturn(accessCookie);

        when(cookieService.generateAccessTokenCookie(refreshToken))
                .thenReturn(refreshCookie);

        Map<String, ResponseCookie> result =
                authService.authenticate(loginRequest);

        assertThat(result)
                .containsOnlyKeys(
                        "accessTokenCookie",
                        "refreshTokenCookie"
                );

        assertThat(result.get("accessTokenCookie"))
                .isSameAs(accessCookie);

        assertThat(result.get("refreshTokenCookie"))
                .isSameAs(refreshCookie);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(
                        UsernamePasswordAuthenticationToken.class
                );

        verify(authenticationManager)
                .authenticate(captor.capture());

        UsernamePasswordAuthenticationToken authenticationToken =
                captor.getValue();

        assertThat(authenticationToken.getPrincipal())
                .isEqualTo(loginRequest.email());

        assertThat(authenticationToken.getCredentials())
                .isEqualTo(loginRequest.password());

        verify(jwtTokenService)
                .generateAccessToken(userDetails);

        verify(jwtTokenService)
                .generateRefreshToken(userDetails);

        verify(cookieService)
                .generateAccessTokenCookie(accessToken);

        verify(cookieService)
                .generateAccessTokenCookie(refreshToken);
    }

    @Test
    void shouldPropagateAuthenticationException() {
        LoginRequestDto loginRequest =
                new LoginRequestDto(
                        "john.doe@test.com",
                        "WrongPassword1!"
                );

        AuthenticationException exception =
                new AuthenticationException(
                        "Authentication failed"
                ) {
                };

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(exception);

        assertThatThrownBy(() ->
                authService.authenticate(loginRequest)
        )
                .isSameAs(exception);

        verify(authenticationManager)
                .authenticate(
                        any(UsernamePasswordAuthenticationToken.class)
                );

        verify(jwtTokenService, never())
                .generateAccessToken(any());

        verify(jwtTokenService, never())
                .generateRefreshToken(any());

        verify(cookieService, never())
                .generateAccessTokenCookie(any());
    }

    @Test
    void shouldRefreshAccessTokenSuccessfully() throws Exception {
        Long userId = 42L;

        String refreshToken = "refresh-token";
        String accessToken = "new-access-token";

        Cookie refreshCookie =
                new Cookie(
                        "refresh_token",
                        refreshToken
                );

        Jwt jwt = mockRefreshJwt(userId);

        ResponseCookie accessTokenCookie =
                ResponseCookie
                        .from("accessToken", accessToken)
                        .build();

        when(request.getCookies())
                .thenReturn(new Cookie[]{refreshCookie});

        when(jwtTokenService.decodeAndValidate(refreshToken))
                .thenReturn(jwt);

        when(userDetailsServiceImpl.loadUserByUserId(userId))
                .thenReturn(userDetails);

        when(jwtTokenService.generateAccessToken(userDetails))
                .thenReturn(accessToken);

        when(cookieService.generateAccessTokenCookie(accessToken))
                .thenReturn(accessTokenCookie);

        ResponseCookie result =
                authService.refreshAccessToken(request);

        assertThat(result)
                .isSameAs(accessTokenCookie);

        verify(jwtTokenService)
                .decodeAndValidate(refreshToken);

        verify(userDetailsServiceImpl)
                .loadUserByUserId(userId);

        verify(jwtTokenService)
                .generateAccessToken(userDetails);

        verify(cookieService)
                .generateAccessTokenCookie(accessToken);
    }

    @Test
    void shouldRejectRefreshWhenRefreshCookieIsMissing() {
        when(request.getCookies())
                .thenReturn(null);

        assertThatThrownBy(() ->
                authService.refreshAccessToken(request)
        )
                .isInstanceOf(InvalidTokenException.class);

        verify(jwtTokenService, never())
                .decodeAndValidate(any());

        verify(userDetailsServiceImpl, never())
                .loadUserByUserId(any());

        verify(jwtTokenService, never())
                .generateAccessToken(any());
    }

    @Test
    void shouldRejectRefreshWhenRefreshCookieHasInvalidName() {
        Cookie accessCookie =
                new Cookie(
                        "access_token",
                        "some-token"
                );

        when(request.getCookies())
                .thenReturn(new Cookie[]{accessCookie});

        assertThatThrownBy(() ->
                authService.refreshAccessToken(request)
        )
                .isInstanceOf(InvalidTokenException.class);

        verify(jwtTokenService, never())
                .decodeAndValidate(any());
    }

    @Test
    void shouldRejectRefreshWhenTokenTypeIsInvalid() {
        String refreshToken = "refresh-token";

        Cookie refreshCookie =
                new Cookie(
                        "refresh_token",
                        refreshToken
                );

        Jwt jwt = mock(Jwt.class);

        when(request.getCookies())
                .thenReturn(new Cookie[]{refreshCookie});

        when(jwtTokenService.decodeAndValidate(refreshToken))
                .thenReturn(jwt);

        when(jwt.getClaims())
                .thenReturn(Map.of("type", "access"));

        assertThatThrownBy(() ->
                authService.refreshAccessToken(request)
        )
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Invalid token type");

        verify(jwtTokenService)
                .decodeAndValidate(refreshToken);

        verify(jwt)
                .getClaims();

        verify(userDetailsServiceImpl, never())
                .loadUserByUserId(any());

        verify(jwtTokenService, never())
                .generateAccessToken(any());

        verify(cookieService, never())
                .generateAccessTokenCookie(any());
    }

    @Test
    void shouldRejectRefreshWhenCookiesArrayIsEmpty() {
        when(request.getCookies())
                .thenReturn(new Cookie[0]);

        assertThatThrownBy(() ->
                authService.refreshAccessToken(request)
        )
                .isInstanceOf(InvalidTokenException.class);

        verify(jwtTokenService, never())
                .decodeAndValidate(any());
    }

    @Test
    void shouldRegisterSuccessfullyWithoutMedia() {
        RegisterRequestDto registerRequest =
                new RegisterRequestDto(
                        "john.doe@test.com",
                        "Password1!",
                        "john",
                        "John",
                        "Doe"
                );

        authService.register(
                registerRequest,
                null
        );

        verify(userService)
                .register(
                        registerRequest,
                        null
                );
    }

    @Test
    void shouldRegisterSuccessfullyWithMedia() {
        RegisterRequestDto registerRequest =
                new RegisterRequestDto(
                        "john.doe@test.com",
                        "Password1!",
                        "john",
                        "John",
                        "Doe"
                );

        MockMultipartFile media = MediaTestFactory.createMedia();

        authService.register(
                registerRequest,
                media
        );

        verify(userService)
                .register(
                        registerRequest,
                        media
                );
    }

    @Test
    void shouldPropagateExceptionWhenRegistrationFails() {
        RegisterRequestDto registerRequest =
                new RegisterRequestDto(
                        "john.doe@test.com",
                        "Password1!",
                        "john",
                        "John",
                        "Doe"
                );

        RuntimeException exception =
                new RuntimeException(
                        "Registration failed"
                );

        doThrow(exception)
                .when(userService)
                .register(
                        registerRequest,
                        null
                );

        assertThatThrownBy(() ->
                authService.register(
                        registerRequest,
                        null
                )
        )
                .isSameAs(exception);

        verify(userService)
                .register(
                        registerRequest,
                        null
                );
    }

    @Test
    void shouldReturnUsernameAvailability() {
        String username = "john";

        when(userService.usernameAvailable(username))
                .thenReturn(true);

        UsernameAvailableDto result =
                authService.usernameAvailable(username);

        assertThat(result)
                .isNotNull();

        assertThat(result.exist())
                .isTrue();

        verify(userService)
                .usernameAvailable(username);
    }

    @Test
    void shouldReturnUsernameUnavailable() {
        String username = "john";

        when(userService.usernameAvailable(username))
                .thenReturn(false);

        UsernameAvailableDto result =
                authService.usernameAvailable(username);

        assertThat(result)
                .isNotNull();

        assertThat(result.exist())
                .isFalse();

        verify(userService)
                .usernameAvailable(username);
    }

    @Test
    void shouldReturnLogoutCookies() {
        ResponseCookie accessCookie =
                ResponseCookie
                        .from("accessToken", "")
                        .maxAge(0)
                        .build();

        ResponseCookie refreshCookie =
                ResponseCookie
                        .from("refreshToken", "")
                        .maxAge(0)
                        .build();

        Map<String, ResponseCookie> cookies =
                Map.of(
                        "accessCookie",
                        accessCookie,
                        "refreshCookie",
                        refreshCookie
                );

        when(cookieService.generateLogoutCookies())
                .thenReturn(cookies);

        Map<String, ResponseCookie> result =
                authService.logout();

        assertThat(result)
                .isSameAs(cookies);

        assertThat(result)
                .containsEntry(
                        "accessCookie",
                        accessCookie
                )
                .containsEntry(
                        "refreshCookie",
                        refreshCookie
                );

        verify(cookieService)
                .generateLogoutCookies();
    }

    private Jwt mockRefreshJwt(Long userId) {
        Jwt jwt = mock(Jwt.class);

        when(jwt.getSubject())
                .thenReturn(String.valueOf(userId));

        when(jwt.getClaims())
                .thenReturn(
                        Map.of("type", "refresh")
                );

        return jwt;
    }
}

