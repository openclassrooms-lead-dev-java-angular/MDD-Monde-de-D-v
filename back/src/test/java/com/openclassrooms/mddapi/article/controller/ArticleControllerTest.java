package com.openclassrooms.mddapi.article.controller;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.dto.ArticleUpdateRequestDto;
import com.openclassrooms.mddapi.article.exception.ArticleNotFoundException;
import com.openclassrooms.mddapi.article.security.ArticleSecurity;
import com.openclassrooms.mddapi.article.service.ArticleService;
import com.openclassrooms.mddapi.common.dto.AvailableSlugDto;
import com.openclassrooms.mddapi.factory.ArticleTestFactory;
import com.openclassrooms.mddapi.factory.MediaTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleService articleService;

    @MockitoBean(name = "articleSecurity")
    private ArticleSecurity articleSecurity;

    @BeforeEach
    void setUp() {
        when(articleSecurity.isAuthor(anyString()))
                .thenReturn(true);
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/articles
    // -------------------------------------------------------------------------

    @Test
    void shouldGetAllArticles() throws Exception {
        ArticleResponseDto article = ArticleTestFactory.createArticleResponseDto(false);

        PageImpl<ArticleResponseDto> page = new PageImpl<>(
                List.of(article),
                PageRequest.of(0, 10),
                1
        );

        when(articleService.findAll(any()))
                .thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/articles")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.content")
                                .isArray())
                .andExpect(
                        jsonPath("$.content[0].username")
                                .value("Jane_Doe")
                )
                .andExpect(
                        jsonPath("$.content[0].title")
                                .value("article title"))
                .andExpect(
                        jsonPath("$.content[0].slug")
                                .value("my-article"));

        verify(articleService)
                .findAll(any());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/articles/{slug}
    // -------------------------------------------------------------------------

    @Test
    void shouldGetArticleBySlug() throws Exception {

        ArticleResponseDto article = ArticleTestFactory.createArticleResponseDto(false);

        when(articleService.findBySlug("my-article"))
                .thenReturn(article);

        mockMvc.perform(get("/api/v1/articles/my-article"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.username")
                                .value("Jane_Doe"))
                .andExpect(
                        jsonPath("$.title")
                                .value("article title"))
                .andExpect(
                        jsonPath("$.slug")
                                .value("my-article"))
                .andExpect(
                        jsonPath("$.content")
                                .value("my article content"))
                .andExpect(
                        jsonPath("$.createdAt")
                                .exists())
                .andExpect(
                        jsonPath("$.updatedAt")
                                .exists());

        verify(articleService)
                .findBySlug("my-article");
    }

    @Test
    void shouldReturn404WhenArticleDoesNotExist() throws Exception {
        when(articleService.findBySlug("article-unknown"))
                .thenThrow(new ArticleNotFoundException("Article not found with slug: article-unknown"));

        mockMvc.perform(
                        get("/api/v1/articles/article-unknown"))
                .andExpect(status().isNotFound());

        verify(articleService).findBySlug("article-unknown");
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/articles
    // -------------------------------------------------------------------------
    @Test
    void shouldCreateArticleWithoutMedia() throws Exception {
        ArticleResponseDto response = ArticleTestFactory.createArticleResponseDto(false);

        when(articleService.create(any(ArticleRequestDto.class), any()))
                .thenReturn(response);

        MockMultipartFile articlePart = ArticleTestFactory.createArticlePart();

        mockMvc.perform(
                        multipart("/api/v1/articles")
                                .file(articlePart)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.title")
                                .value("article title"))
                .andExpect(
                        jsonPath("$.slug")
                                .value("my-article"))
                .andExpect(
                        jsonPath("$.content")
                                .value("my article content"))
                .andExpect(
                        jsonPath("$.media")
                                .isEmpty()
                );
        verify(articleService).create(any(ArticleRequestDto.class), any());
    }

    @Test
    void shouldCreateArticleWithMedia() throws Exception {
        ArticleResponseDto response = ArticleTestFactory.createArticleResponseDto(true);

        when(articleService.create(
                any(ArticleRequestDto.class),
                any(MockMultipartFile.class)
        ))
                .thenReturn(response);

        MockMultipartFile articlePart = ArticleTestFactory.createArticlePart();

        MockMultipartFile media = MediaTestFactory.createMedia();

        mockMvc.perform(
                        multipart("/api/v1/articles")
                                .file(articlePart)
                                .file(media)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.media")
                                .value("articles/1/image.jpg"));

        verify(articleService)
                .create(
                        any(ArticleRequestDto.class),
                        any(MockMultipartFile.class));
    }

    @Test
    void shouldReturn500WhenArticleRequestIsInvalid() throws Exception {
        MockMultipartFile articlePart = new MockMultipartFile(
                "article",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """ 
                                { 
                                    "title": "", 
                                    "slug": "", 
                                    "content": 
                                    "my article content", 
                                    "topicSlug": "" 
                                } 
                        """.getBytes());

        mockMvc.perform(
                        multipart("/api/v1/articles")
                                .file(articlePart)
                )
                .andExpect(status().isInternalServerError());

        verify(articleService, never())
                .create(any(), any());
    }

    @Test
    void shouldReturn500WhenArticlePartIsMissing() throws Exception {
        mockMvc.perform(
                        multipart("/api/v1/articles")
                )
                .andExpect(status().isInternalServerError());

        verify(articleService, never())
                .create(any(), any());
    }

    // -------------------------------------------------------------------------
    // PUT /api/v1/articles/{slug}
    // -------------------------------------------------------------------------

    @Test
    void shouldUpdateArticle() throws Exception {
        ArticleResponseDto response = ArticleTestFactory.createUpdatedArticleResponseDto(false);

        when(articleService.update(
                eq("my-article"),
                any(ArticleUpdateRequestDto.class),
                MediaTestFactory.createMedia())
        )
                .thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/articles/my-article")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(""" 
                                        { 
                                        "title": "Updated article", 
                                        "slug": "updated-article", 
                                        "content": "New article content", 
                                        "topicSlug": "java",
                                        "updatedMedia": false
                                        } 
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.title")
                                .value("Updated article"))
                .andExpect(
                        jsonPath("$.slug")
                                .value("updated-article"))
                .andExpect(
                        jsonPath("$.content")
                                .value("New article content"))
                .andExpect(
                        jsonPath("$.createdAt")
                                .exists())
                .andExpect(
                        jsonPath("$.updatedAt")
                                .exists());

        verify(articleService)
                .update(
                        eq("my-article"),
                        any(ArticleUpdateRequestDto.class),
                        MediaTestFactory.createMedia()
                );
    }

    @Test
    void shouldReturn500WhenUpdateRequestIsInvalid() throws Exception {
        mockMvc.perform(
                        put("/api/v1/articles/mon-article")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(""" 
                                                { 
                                                    "title": "", 
                                                    "slug": "", 
                                                    "content": "", 
                                                    "topicSlug": "" 
                                                } 
                                        """)
                )
                .andExpect(status().isInternalServerError());

        verify(articleService, never())
                .update(any(), any(), any());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/articles/available-slug/{slug}
    // -------------------------------------------------------------------------

    @Test
    void shouldCheckSlugAvailability() throws Exception {
        AvailableSlugDto response = new AvailableSlugDto(true);

        when(articleService.findAvailableSlug("new-article"))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/articles/available-slug/new-article")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.available")
                                .value(true));

        verify(articleService)
                .findAvailableSlug("new-article");
    }

    @Test
    void shouldReturnUnavailableSlug() throws Exception {
        AvailableSlugDto response = new AvailableSlugDto(false);

        when(articleService.findAvailableSlug("existing-article"))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/articles/available-slug/existing-article")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.available")
                                .value(false));

        verify(articleService)
                .findAvailableSlug("existing-article");
    }
}
