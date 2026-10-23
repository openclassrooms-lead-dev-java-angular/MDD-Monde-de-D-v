package com.openclassrooms.mddapi.security;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public final class TestAuthentication {

    private TestAuthentication() {
    }

    public static RequestPostProcessor authenticatedUser() {
        var principal = new TestPrincipal(42L);

        var authentication =
                new org.springframework.security.authentication
                        .UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        java.util.Collections.emptyList()
                );

        return SecurityMockMvcRequestPostProcessors.authentication(authentication);
    }
}
