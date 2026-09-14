package com.openclassrooms.mddapi.auth.security.cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class CookieServiceTest {

    private CookieService cookieService;

    @BeforeEach
    void setUp() {
        cookieService = new CookieService();
        ReflectionTestUtils.setField(cookieService, "secureCookie", true);
        ReflectionTestUtils.setField(cookieService, "accessTokenExpiration", 15);
        ReflectionTestUtils.setField(cookieService, "refreshTokenExpiration", 30);
    }

    @Test
    void shouldGenerateAccessTokenCookie() {
        // Given
        String token = "access-token-value";

        // When
        ResponseCookie cookie = cookieService.generateAccessTokenCookie(token);

        // Then
        assertThat(cookie).isNotNull();
        assertThat(cookie.getName()).isEqualTo("access_token");
        assertThat(cookie.getValue()).isEqualTo(token);
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.isHttpOnly()).isFalse();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()) .isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void shouldGenerateRefreshTokenCookie() {
        // Given
        String token = "refresh-token-value";

        // When
        ResponseCookie cookie = cookieService.generateRefreshTokenCookie(token);

        // Then
        assertThat(cookie).isNotNull();
        assertThat(cookie.getName()).isEqualTo("refresh_token");
        assertThat(cookie.getValue()).isEqualTo(token);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
        assertThat(cookie.getPath()).isEqualTo("/api/v1/auth");
        assertThat(cookie.getMaxAge()) .isEqualTo(Duration.ofDays(30));
    }

    @Test
    void shouldGenerateLogoutCookies() {
        // When
        Map<String, ResponseCookie> cookies = cookieService.generateLogoutCookies();

        // Then
        assertThat(cookies)
                .isNotNull()
                .hasSize(2);
        ResponseCookie accessCookie = cookies.get("accessCookie");

        assertThat(accessCookie).isNotNull();
        assertThat(accessCookie.getName()).isEqualTo("access_token");
        assertThat(accessCookie.getValue()).isEmpty();
        assertThat(accessCookie.isHttpOnly()).isTrue();
        assertThat(accessCookie.isSecure()).isTrue();
        assertThat(accessCookie.getSameSite()).isEqualTo("Lax");
        assertThat(accessCookie.getPath()).isEqualTo("/");
        assertThat(accessCookie.getMaxAge()).isEqualTo(Duration.ZERO);

        ResponseCookie refreshCookie = cookies.get("refreshCookie");

        assertThat(refreshCookie).isNotNull();
        assertThat(refreshCookie.getName()).isEqualTo("refresh_token");
        assertThat(refreshCookie.getValue()).isEmpty();
        assertThat(refreshCookie.isHttpOnly()).isTrue();
        assertThat(refreshCookie.isSecure()).isTrue();
        assertThat(refreshCookie.getSameSite()).isEqualTo("Lax");
        assertThat(refreshCookie.getPath()).isEqualTo("/api/v1/auth");
        assertThat(refreshCookie.getMaxAge()).isEqualTo(Duration.ZERO);
    }

    @Test
    void shouldGenerateInsecureCookiesWhenSecureCookieIsDisabled() {
        // Given
        ReflectionTestUtils.setField(cookieService, "secureCookie", false);

        // When
        ResponseCookie accessCookie = cookieService.generateAccessTokenCookie("access-token");
        ResponseCookie refreshCookie = cookieService.generateRefreshTokenCookie("refresh-token");
        Map<String, ResponseCookie> logoutCookies = cookieService.generateLogoutCookies();

        // Then
        assertThat(accessCookie.isSecure()).isFalse();
        assertThat(refreshCookie.isSecure()).isFalse();
        assertThat(logoutCookies.get("accessCookie").isSecure()).isFalse();
        assertThat(logoutCookies.get("refreshCookie").isSecure()).isFalse();
    }
}
