
package com.openclassrooms.mddapi.auth.security.csrf;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class SpaCsrfTokenRequestHandlerTest {

    private SpaCsrfTokenRequestHandler handler;
    private CsrfToken csrfToken;

    @BeforeEach
    void setUp() {
        handler = new SpaCsrfTokenRequestHandler();
        csrfToken = new DefaultCsrfToken(
                "X-XSRF-TOKEN",
                "_csrf",
                "test-token"
        );
    }

    @Test
    void shouldLoadDeferredTokenWhenHandlingRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicInteger calls = new AtomicInteger();

        Supplier<CsrfToken> tokenSupplier = () -> {
            calls.incrementAndGet();
            return csrfToken;
        };

        handler.handle(request, response, tokenSupplier);

        assertThat(calls.get()).isEqualTo(1);
        assertThat(request.getAttribute("_csrf")).isNotNull();
    }

    @Test
    void shouldResolveTokenFromPlainHeaderWhenHeaderIsPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-XSRF-TOKEN", "plain-token");

        String result = handler.resolveCsrfTokenValue(request, csrfToken);

        assertThat(result).isEqualTo("plain-token");
    }

    @Test
    void shouldResolveTokenUsingXorHandlerWhenHeaderIsMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = handler.resolveCsrfTokenValue(request, csrfToken);

        assertThat(result).isNull();
    }

    @Test
    void shouldUseXorHandlerWhenHeaderIsBlank() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-XSRF-TOKEN", "   ");

        String result = handler.resolveCsrfTokenValue(request, csrfToken);

        assertThat(result).isNull();
    }
}
