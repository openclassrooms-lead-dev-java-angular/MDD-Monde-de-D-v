package com.openclassrooms.mddapi.article.controller;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.security.ArticleSecurity;
import com.openclassrooms.mddapi.article.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(ArticleController.class)
@ActiveProfiles("test")
@Import(ArticleControllerTest.TestSecurityConfig.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleService articleService;

    @MockitoBean(name = "articleSecurity")
    private ArticleSecurity articleSecurity;

    /*
     * Creates an authenticated principal for controller tests.
     */
    static class TestPrincipal {

        private final Long id;

        TestPrincipal(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }

    private RequestPostProcessor authenticatedUser() {
        var principal = new TestPrincipal(42L);

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );

        return SecurityMockMvcRequestPostProcessors
                .authentication(authentication);
    }

    /*
     * GET /api/v1/articles/feed
     */

    @Test
    void getFeedArticles_shouldReturnEmptyPage() throws Exception {
        when(articleService.findFeedArticles(any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/articles/feed")
                        .with(authenticatedUser())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(articleService).findFeedArticles(any());
    }

    /*
     * GET /api/v1/articles/{publicId}
     */

    @Test
    void getArticleByPublicId_shouldReturnArticleWhenAuthorized()
            throws Exception {

        String publicId = "article-public-id";

        when(articleSecurity.canAccessArticle(publicId))
                .thenReturn(true);

        when(articleService.findByPublicId(publicId))
                .thenReturn(mock(ArticleResponseDto.class));

        mockMvc.perform(get("/api/v1/articles/{publicId}", publicId)
                        .with(authenticatedUser()))
                .andExpect(status().isOk());

        verify(articleSecurity).canAccessArticle(publicId);
        verify(articleService).findByPublicId(publicId);
    }

    @Test
    void getArticleByPublicId_shouldReturnForbiddenWhenUnauthorized()
            throws Exception {

        String publicId = "private-article-public-id";

        when(articleSecurity.canAccessArticle(publicId))
                .thenReturn(false);

        mockMvc.perform(get("/api/v1/articles/{publicId}", publicId)
                        .with(authenticatedUser()))
                .andExpect(status().isForbidden());

        verify(articleSecurity).canAccessArticle(publicId);
        verify(articleService, never()).findByPublicId(anyString());
    }

    /*
     * POST /api/v1/articles
     *
     * Update the JSON fields to match ArticleRequestDto.
     */

    @Test
    void createArticle_shouldCreateArticleWithValidCsrf()
            throws Exception {

        String validJson = """
                {
                    "title": "My test article",
                    "content": "Test content",
                    "topicPublicId": "topic-public-id"
                }
                """;

        when(articleService.create(any(ArticleRequestDto.class)))
                .thenReturn(mock(ArticleResponseDto.class));

        mockMvc.perform(post("/api/v1/articles")
                        .with(authenticatedUser())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk());

        verify(articleService).create(any(ArticleRequestDto.class));
    }

    @Test
    void createArticle_shouldReturnForbiddenWithoutCsrf()
            throws Exception {

        String validJson = """
                {
                    "title": "My test article",
                    "content": "Test content",
                    "topicPublicId": "topic-public-id"
                }
                """;

        mockMvc.perform(post("/api/v1/articles")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isForbidden());

        verify(articleService, never())
                .create(any(ArticleRequestDto.class));
    }

    /*
     * Test security configuration with CSRF and method security enabled.
     */

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http)
                throws Exception {

            http
                    .csrf(csrf -> {})
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().authenticated()
                    );

            return http.build();
        }
    }
}