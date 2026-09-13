package com.openclassrooms.mddapi.auth.security.cookie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class CookieService {

    @Value("${app.jwt.cookie.secure}")
    private boolean secureCookie;

    @Value("${app.jwt.expiration.access-token}")
    private int accessTokenExpiration;

    @Value("${app.jwt.expiration.refresh-token}")
    private int refreshTokenExpiration;

    /**
     * Creates an HTTP cookie containing the access token.
     *
     * <p>The cookie is configured with the application's security settings,
     * including the {@code Secure} flag, {@code SameSite=Lax} policy,
     * root path and configured access token expiration time.</p>
     *
     * @param token the JWT access token to store in the cookie
     * @return the configured access token cookie
     */
    public ResponseCookie generateAccessTokenCookie(String token) {
        return ResponseCookie
                .from("access_token", token)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMinutes(accessTokenExpiration))
                .build();

    }

    /**
     * Creates an HTTP-only cookie containing the refresh token.
     *
     * <p>The refresh token cookie is restricted to the authentication endpoints
     * through its path and is configured with the application's security settings,
     * including the {@code HttpOnly}, {@code Secure} and {@code SameSite=Lax}
     * attributes.</p>
     *
     * @param token the JWT refresh token to store in the cookie
     * @return the configured refresh token cookie
     */
    public ResponseCookie generateRefreshTokenCookie(String token) {
        return ResponseCookie
                .from("refresh_token", token)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofDays(refreshTokenExpiration))
                .build();
    }

    /**
     * Creates the cookies required to log out the current user.
     *
     * <p>The returned cookies overwrite the existing access and refresh token
     * cookies with empty values and an immediate expiration time, causing the
     * browser to remove them.</p>
     *
     * @return a map containing the expired access and refresh token cookies,
     * keyed by {@code accessCookie} and {@code refreshCookie}
     */
    public Map<String, ResponseCookie> generateLogoutCookies() {
        ResponseCookie accessCookie = ResponseCookie
                .from("access_token", "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();

        return Map.of(
                "accessCookie", accessCookie,
                "refreshCookie", refreshCookie
        );
    }
}
