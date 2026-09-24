package com.openclassrooms.mddapi.auth.service;

import com.openclassrooms.mddapi.auth.security.cookie.CookieService;
import com.openclassrooms.mddapi.auth.dto.LoginRequestDto;
import com.openclassrooms.mddapi.auth.dto.RegisterRequestDto;
import com.openclassrooms.mddapi.auth.dto.UsernameAvailableDto;
import com.openclassrooms.mddapi.auth.security.jwt.JwtTokenService;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsImpl;
import com.openclassrooms.mddapi.auth.security.userDetails.UserDetailsServiceImpl;
import com.openclassrooms.mddapi.auth.exception.InvalidTokenException;
import com.openclassrooms.mddapi.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 * Service responsible for authentication, registration, token refresh,
 * username availability checks and logout operations.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final UserService userService;
    private final CookieService cookieService;

    /**
     * Authenticates a user using their email and password and generates
     * access and refresh token cookies.
     *
     * @param loginRequest the user's login credentials
     * @return a map containing the access token and refresh token cookies
     * @throws org.springframework.security.core.AuthenticationException if the provided credentials are invalid
     */
    @Transactional(readOnly = true)
    public Map<String, ResponseCookie> authenticate(
            LoginRequestDto loginRequest
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String accessToken = jwtTokenService.generateAccessToken(userDetails);
        String refreshToken = jwtTokenService.generateRefreshToken(userDetails);

        ResponseCookie accessTokenCookie = cookieService.generateAccessTokenCookie(accessToken);
        ResponseCookie refreshTokenCookie = cookieService.generateAccessTokenCookie(refreshToken);

        return Map.of(
                "accessTokenCookie", accessTokenCookie,
                "refreshTokenCookie", refreshTokenCookie
        );
    }

    /**
     * Generates a new access token from the refresh token stored in the * request cookies.
     *
     * @param request the HTTP request containing the refresh token cookie
     * @return a cookie containing the newly generated access token
     * @throws InvalidTokenException if the refresh token is missing,
     * invalid or has an incorrect token type
     */
    @Transactional(readOnly = true)
    public ResponseCookie refreshAccessToken(HttpServletRequest request) {

        String refreshToken = Arrays.stream(Optional.ofNullable(request.getCookies())
                        .orElse(new Cookie[0])
                )
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElseThrow(InvalidTokenException::new);

        Jwt jwt = jwtTokenService.decodeAndValidate(refreshToken);

        if (!"refresh".equals(jwt.getClaims().get("type"))) {
            throw new InvalidTokenException("Invalid token type");
        }

        Long userId = Long.valueOf(jwt.getSubject());
        UserDetailsImpl userDetails = userDetailsServiceImpl.loadUserByUserId(userId);

        String token = jwtTokenService.generateAccessToken(userDetails);

        return cookieService.generateAccessTokenCookie(token);
    }

    /**
     *  Registers a new user.
     *
     * @param registerRequestDto the data required to register the user
     */
    @Transactional
    public void register(
            RegisterRequestDto registerRequestDto,
            final MultipartFile media
    ) {
        userService.register(registerRequestDto, media);
    }

    /**
     * Checks whether a username is available for registration.
     *
     * @param username the username to check
     * @return an object indicating whether the username is available
     */
    @Transactional(readOnly = true)
    public UsernameAvailableDto usernameAvailable(String username) {
        boolean usernameExists = userService.usernameAvailable(username);

        return new UsernameAvailableDto(usernameExists);
    }

    /**
     * Generates cookies that invalidate the authentication tokens,
     * effectively logging the user out.
     *
     * @return a map containing the logout cookies
     */
    @Transactional(readOnly = true)
    public Map<String, ResponseCookie> logout() {
        return cookieService.generateLogoutCookies();
    }
}
