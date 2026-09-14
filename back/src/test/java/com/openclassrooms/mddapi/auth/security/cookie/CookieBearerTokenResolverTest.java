package com.openclassrooms.mddapi.auth.security.cookie;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


public class CookieBearerTokenResolverTest {

    private CookieBearerTokenResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new CookieBearerTokenResolver();
    }

    @Test
    void shouldReturnTokenWhenAccessTokenCookieExists() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("access_token", "jwt-token")
        );

        String result = resolver.resolve(request);

        assertEquals("jwt-token", result);
    }

    @Test
    void shouldReturnNullWhenRequestHasNoCookies() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = resolver.resolve(request);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenAccessTokenCookieDoesNotExist() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("other_cookie", "some-value")
        );

        String result = resolver.resolve(request);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenAccessTokenCookieIsEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("access_token", "")
        );

        String result = resolver.resolve(request);

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenAccessTokenCookieIsBlank() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("access_token", "   ")
        );

        String result = resolver.resolve(request);

        assertNull(result);
    }

    @Test
    void shouldReturnAccessTokenWhenSeveralCookiesExist() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("session", "abc"),
                new Cookie("access_token", "jwt-token"),
                new Cookie("other", "xyz")
        );

        String result = resolver.resolve(request);

        assertEquals("jwt-token", result);
    }

    @Test
    void shouldIgnoreEmptyAccessTokenAndUseValidOne() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
                new Cookie("access_token", ""),
                new Cookie("access_token", "jwt-token")
        );

        String result = resolver.resolve(request);

        assertEquals("jwt-token", result);
    }
}
