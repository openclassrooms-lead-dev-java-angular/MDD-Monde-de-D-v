package com.openclassrooms.mddapi.common.config;

import com.openclassrooms.mddapi.common.validation.ValidatingPageableResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class PageableConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(
            List<HandlerMethodArgumentResolver> resolvers
    ) {
        ValidatingPageableResolver resolver =
                new ValidatingPageableResolver();

        resolver.setPageParameterName("page");
        resolver.setSizeParameterName("size");

        resolver.setMaxPageSize(100);
        resolver.setOneIndexedParameters(false);

        resolvers.add(resolver);
    }
}
