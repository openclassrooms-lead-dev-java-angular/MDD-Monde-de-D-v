package com.openclassrooms.mddapi.auth.service;

import com.openclassrooms.mddapi.auth.security.cookie.CookieService;
import com.openclassrooms.mddapi.auth.dto.LoginRequestDto;
import com.openclassrooms.mddapi.auth.dto.RegisterRequestDto;
import com.openclassrooms.mddapi.auth.dto.UsernameAvailableDto;
import com.openclassrooms.mddapi.auth.exception.UnauthorizedException;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final UserService userService;
    private final CookieService cookieService;

    @Transactional(readOnly = true)
    public Map<String, ResponseCookie> authenticate(
            LoginRequestDto loginRequest
    ) {
        System.out.println("authentication before");
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.email(),
                        loginRequest.password()
                )
        );
        System.out.println("authentication");
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        System.out.println("after authentication");

        String accessToken = jwtTokenService.generateAccessToken(userDetails);
        String refreshToken = jwtTokenService.generateRefreshToken(userDetails);

        System.out.println(accessToken);
        System.out.println(refreshToken);

        ResponseCookie accessTokenCookie = cookieService.generateAccessTokenCookie(accessToken);
        ResponseCookie refreshTokenCookie = cookieService.generateAccessTokenCookie(refreshToken);

        System.out.println(accessTokenCookie);
        System.out.println(refreshTokenCookie);

        return Map.of(
                "accessTokenCookie", accessTokenCookie,
                "refreshTokenCookie", refreshTokenCookie
        );
    }

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

    @Transactional
    public void register(RegisterRequestDto registerRequestDto) {
        userService.register(registerRequestDto);
    }

    @Transactional(readOnly = true)
    public UsernameAvailableDto usernameAvailable(String username) {
        boolean usernameExists = userService.usernameAvailable(username);

        return new UsernameAvailableDto(usernameExists);
    }


    @Transactional(readOnly = true)
    public Map<String, ResponseCookie> logout() {
        return cookieService.generateLogoutCookies();
    }

    @Transactional(readOnly = true)
    public Long getPrincipalUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        if (!(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            throw new UnauthorizedException("Invalid authenticated user");
        }

        return userDetails.getId();
    }
}
