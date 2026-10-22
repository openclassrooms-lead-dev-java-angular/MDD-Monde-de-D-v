package com.openclassrooms.mddapi.auth.service;

import com.openclassrooms.mddapi.auth.dto.LoginRequestDto;
import com.openclassrooms.mddapi.auth.dto.RegisterRequestDto;
import com.openclassrooms.mddapi.auth.dto.UsernameAvailableDto;
import com.openclassrooms.mddapi.auth.security.cookie.CookieService;
import com.openclassrooms.mddapi.auth.security.jwt.JwtTokenService;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsImpl;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.auth.exception.InvalidTokenException;
import com.openclassrooms.mddapi.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.ResponseCookie;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
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

    // ---------------------
    // authenticate
    // ---------------------

    @Test
    void shouldAuthenticateSuccessfully() {
        LoginRequestDto loginRequest = new LoginRequestDto(
                "john.doe@test.com",
                "Password1!"
        );

        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        ResponseCookie accessCookie = ResponseCookie
                .from("accessToken", accessToken)
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", refreshToken)
                .build();

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(jwtTokenService.generateAccessToken(userDetails))
                .thenReturn(accessToken);

        when(jwtTokenService.generateRefreshToken(userDetails))
                .thenReturn(refreshToken);

        when(cookieService.generateAccessTokenCookie(accessToken))
                .thenReturn(accessCookie);

        when(cookieService.generateRefreshTokenCookie(refreshToken))
                .thenReturn(refreshCookie);

        Map<String, ResponseCookie> result =
                authService.authenticate(loginRequest);

        assertEquals(
                Set.of("accessTokenCookie", "refreshTokenCookie"),
                result.keySet()
        );
        assertSame(accessCookie, result.get("accessTokenCookie"));
        assertSame(refreshCookie, result.get("refreshTokenCookie"));

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(
                        UsernamePasswordAuthenticationToken.class
                );

        verify(authenticationManager).authenticate(captor.capture());

        UsernamePasswordAuthenticationToken authenticationToken =
                captor.getValue();

        assertEquals(
                loginRequest.usernameOrEmail(),
                authenticationToken.getPrincipal()
        );
        assertEquals(
                loginRequest.password(),
                authenticationToken.getCredentials()
        );

        verify(jwtTokenService).generateAccessToken(userDetails);
        verify(jwtTokenService).generateRefreshToken(userDetails);
        verify(cookieService).generateAccessTokenCookie(accessToken);
        verify(cookieService).generateRefreshTokenCookie(refreshToken);
    }

    @Test
    void shouldPropagateAuthenticationException() {
        LoginRequestDto loginRequest = new LoginRequestDto(
                "john.doe@test.com",
                "WrongPassword1!"
        );

        AuthenticationException exception = new AuthenticationException(
                "Authentication failed"
        ) {};

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenThrow(exception);

        AuthenticationException thrown = assertThrows(
                AuthenticationException.class,
                () -> authService.authenticate(loginRequest)
        );

        assertSame(exception, thrown);

        verify(authenticationManager).authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        );
        verify(jwtTokenService, never()).generateAccessToken(any());
        verify(jwtTokenService, never()).generateRefreshToken(any());
        verify(cookieService, never()).generateAccessTokenCookie(any());
        verify(cookieService, never()).generateRefreshTokenCookie(any());
    }

    // ---------------------
    // refreshAccessToken
    // ---------------------

    @Test
    void shouldRefreshAccessTokenSuccessfully() {
        Long userId = 42L;
        String refreshToken = "refresh-token";
        String accessToken = "new-access-token";

        Cookie refreshCookie = new Cookie(
                "refresh_token",
                refreshToken
        );

        Jwt jwt = mockRefreshJwt(userId);

        ResponseCookie accessTokenCookie = ResponseCookie
                .from("accessToken", accessToken)
                .build();

        when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});

        when(jwtTokenService.decodeAndValidate(refreshToken))
                .thenReturn(jwt);

        when(userDetailsServiceImpl.loadUserByUserId(userId))
                .thenReturn(userDetails);

        when(jwtTokenService.generateAccessToken(userDetails))
                .thenReturn(accessToken);

        when(cookieService.generateAccessTokenCookie(accessToken))
                .thenReturn(accessTokenCookie);

        ResponseCookie result = authService.refreshAccessToken(request);

        assertSame(accessTokenCookie, result);

        verify(jwtTokenService).decodeAndValidate(refreshToken);
        verify(userDetailsServiceImpl).loadUserByUserId(userId);
        verify(jwtTokenService).generateAccessToken(userDetails);
        verify(cookieService).generateAccessTokenCookie(accessToken);
    }

    private Jwt mockRefreshJwt(Long userId) {
        Jwt jwt = org.mockito.Mockito.mock(Jwt.class);

        when(jwt.getSubject()).thenReturn(String.valueOf(userId));
        when(jwt.getClaims()).thenReturn(Map.of("type", "refresh"));

        return jwt;
    }

    @Test
    void shouldRejectRefreshWhenRefreshCookieIsMissing() {
        when(request.getCookies()).thenReturn(null);

        assertThrows(
                InvalidTokenException.class,
                () -> authService.refreshAccessToken(request)
        );

        verify(jwtTokenService, never()).decodeAndValidate(any());
        verify(userDetailsServiceImpl, never()).loadUserByUserId(any());
        verify(jwtTokenService, never()).generateAccessToken(any());
    }

    @Test
    void shouldRejectRefreshWhenRefreshCookieHasInvalidName() {
        Cookie accessCookie = new Cookie(
                "access_token",
                "some-token"
        );

        when(request.getCookies()).thenReturn(new Cookie[]{accessCookie});

        assertThrows(
                InvalidTokenException.class,
                () -> authService.refreshAccessToken(request)
        );

        verify(jwtTokenService, never()).decodeAndValidate(any());
    }

    @Test
    void shouldRejectRefreshWhenTokenTypeIsInvalid() {
        String refreshToken = "refresh-token";
        Jwt jwt = org.mockito.Mockito.mock(Jwt.class);

        Cookie refreshCookie = new Cookie(
                "refresh_token",
                refreshToken
        );

        when(request.getCookies()).thenReturn(new Cookie[]{refreshCookie});
        when(jwtTokenService.decodeAndValidate(refreshToken)).thenReturn(jwt);
        when(jwt.getClaims()).thenReturn(Map.of("type", "access"));

        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> authService.refreshAccessToken(request)
        );

        assertEquals("Invalid token type", exception.getMessage());

        verify(jwtTokenService).decodeAndValidate(refreshToken);
        verify(userDetailsServiceImpl, never()).loadUserByUserId(any());
        verify(jwtTokenService, never()).generateAccessToken(any());
        verify(cookieService, never()).generateAccessTokenCookie(any());
    }

    @Test
    void shouldRejectRefreshWhenCookiesArrayIsEmpty() {
        when(request.getCookies()).thenReturn(new Cookie[0]);

        assertThrows(
                InvalidTokenException.class,
                () -> authService.refreshAccessToken(request)
        );

        verify(jwtTokenService, never()).decodeAndValidate(any());
    }

    // ---------------------
    // register
    // ---------------------

    @Test
    void shouldRegisterSuccessfully() {
        RegisterRequestDto registerRequest = new RegisterRequestDto(
                "john.doe@test.com",
                "Password1!",
                "john"
        );

        authService.register(registerRequest);

        verify(userService).register(registerRequest);
    }

    @Test
    void shouldPropagateExceptionWhenRegistrationFails() {
        RegisterRequestDto registerRequest = new RegisterRequestDto(
                "john.doe@test.com",
                "Password1!",
                "john"
        );

        RuntimeException exception = new RuntimeException(
                "Registration failed"
        );

        doThrow(exception)
                .when(userService)
                .register(registerRequest);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> authService.register(registerRequest)
        );

        assertSame(exception, thrown);
        verify(userService).register(registerRequest);
    }

    // ---------------------
    // usernameAvailable
    // ---------------------

    @Test
    void shouldReturnUsernameAvailability() {
        String username = "john";

        when(userService.usernameAvailable(username)).thenReturn(true);

        UsernameAvailableDto result =
                authService.usernameAvailable(username);

        assertNotNull(result);
        assertTrue(result.available());

        verify(userService).usernameAvailable(username);
    }

    @Test
    void shouldReturnUsernameUnavailable() {
        String username = "john";

        when(userService.usernameAvailable(username)).thenReturn(false);

        UsernameAvailableDto result =
                authService.usernameAvailable(username);

        assertNotNull(result);
        assertFalse(result.available());

        verify(userService).usernameAvailable(username);
    }

    // ---------------------
    // logout
    // ---------------------

    @Test
    void shouldReturnLogoutCookies() {
        ResponseCookie accessCookie = ResponseCookie
                .from("accessToken", "")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", "")
                .maxAge(0)
                .build();

        Map<String, ResponseCookie> cookies = Map.of(
                "accessCookie", accessCookie,
                "refreshCookie", refreshCookie
        );

        when(cookieService.generateLogoutCookies()).thenReturn(cookies);

        Map<String, ResponseCookie> result = authService.logout();

        assertSame(cookies, result);
        assertEquals(accessCookie, result.get("accessCookie"));
        assertEquals(refreshCookie, result.get("refreshCookie"));

        verify(cookieService).generateLogoutCookies();
    }
}

