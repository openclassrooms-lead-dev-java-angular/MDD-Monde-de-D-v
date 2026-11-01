package com.openclassrooms.mddapi.common.config;

import com.openclassrooms.mddapi.common.validation.ValidatingPageableResolver;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageableConfigTest {

    @Test
    void shouldRegisterValidatingPageableResolver() {
        PageableConfig config = new PageableConfig();

        List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>();

        config.addArgumentResolvers(resolvers);

        assertThat(resolvers).hasSize(1);
        assertThat(resolvers.getFirst())
                .isInstanceOf(ValidatingPageableResolver.class);
    }
}