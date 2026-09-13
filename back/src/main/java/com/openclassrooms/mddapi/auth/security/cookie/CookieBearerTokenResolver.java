package com.openclassrooms.mddapi.auth.security.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private static final String COOKIE_NAME = "access_token";

    /**
     * Resolves the bearer token from the configured HTTP cookie.
     *
     * <p>The method searches the cookies attached to the incoming request for
     * the cookie identified by {@link #COOKIE_NAME} and returns its value.</p>
     *
     * <p>If the request does not contain any cookies, or if the configured
     * cookie is not present, {@code null} is returned.</p>
     *
     * @param request the incoming HTTP request
     * @return the bearer token stored in the configured cookie,
     * or {@code null} if the cookie is not present */
    @Override
    public String resolve(HttpServletRequest request) {

        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
