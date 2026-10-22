package com.openclassrooms.mddapi.article.controller;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.exception.ArticleNotFoundException;
import com.openclassrooms.mddapi.article.service.ArticleService;
import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.common.dto.AvailableSlugDto;
import com.openclassrooms.mddapi.factory.ArticleTestFactory;
import com.openclassrooms.mddapi.factory.LocalDateTimeTestFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    // -------------------------------------------------------------------------
    // GET /api/v1/articles/feed
    // -------------------------------------------------------------------------

    @Test
    void shouldGetFeedArticles() throws Exception {
        ArticleResponseDto article =
                ArticleTestFactory.createArticleResponseDto(false);

        Pageable pageable = PageRequest.of(0, 10);
        Page<ArticleResponseDto> page =
                new PageImpl<>(List.of(article), pageable, 1);

        when(articleService.findFeedArticles(any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/articles/feed")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].username").value("Jane_Doe"))
                .andExpect(jsonPath("$.content[0].title").value("article title"))
                .andExpect(jsonPath("$.content[0].slug").value("my-article"))
                .andExpect(jsonPath("$.content[0].comments").isArray());

        verify(articleService).findFeedArticles(any(Pageable.class));
    }

    @Test
    void shouldReturnEmptyFeed() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);

        when(articleService.findFeedArticles(any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        mockMvc.perform(get("/api/v1/articles/feed")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(articleService).findFeedArticles(any(Pageable.class));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/articles/{slug}
    // -------------------------------------------------------------------------

    @Test
    void shouldGetArticleBySlug() throws Exception {
        ArticleResponseDto article =
                ArticleTestFactory.createArticleResponseDto(false);

        when(articleService.findBySlug("my-article"))
                .thenReturn(article);

        mockMvc.perform(get("/api/v1/articles/my-article"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Jane_Doe"))
                .andExpect(jsonPath("$.title").value("article title"))
                .andExpect(jsonPath("$.slug").value("my-article"))
                .andExpect(jsonPath("$.content").value("my article content"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.comments").isArray());

        verify(articleService).findBySlug("my-article");
    }

    @Test
    void shouldGetArticleBySlugWithComments() throws Exception {
        CommentResponseDto comment1 = mock(CommentResponseDto.class);
        CommentResponseDto comment2 = mock(CommentResponseDto.class);

        ArticleResponseDto article = new ArticleResponseDto(
                "Jane_Doe",
                ArticleTestFactory.createArticleResponseDto(false).topic(),
                "my-article",
                "article title",
                "my article content",
                LocalDateTimeTestFactory.generateCreatedAt(),
                LocalDateTimeTestFactory.generateUpdatedAt(),
                List.of(comment1, comment2)
        );

        when(articleService.findBySlug("my-article"))
                .thenReturn(article);

        mockMvc.perform(get("/api/v1/articles/my-article"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(2));

        verify(articleService).findBySlug("my-article");
    }

    @Test
    void shouldReturnArticleWithEmptyComments() throws Exception {
        ArticleResponseDto original =
                ArticleTestFactory.createArticleResponseDto(false);

        ArticleResponseDto article = new ArticleResponseDto(
                original.username(),
                original.topic(),
                original.slug(),
                original.title(),
                original.content(),
                original.createdAt(),
                original.updatedAt(),
                List.of()
        );

        when(articleService.findBySlug("my-article"))
                .thenReturn(article);

        mockMvc.perform(get("/api/v1/articles/my-article"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments").isEmpty());

        verify(articleService).findBySlug("my-article");
    }

    @Test
    void shouldReturn404WhenArticleDoesNotExist() throws Exception {
        when(articleService.findBySlug("article-unknown"))
                .thenThrow(new ArticleNotFoundException(
                        "Article not found with slug : article-unknown"
                ));

        mockMvc.perform(get("/api/v1/articles/article-unknown"))
                .andExpect(status().isNotFound());

        verify(articleService).findBySlug("article-unknown");
    }

    // -------------------------------------------------------------------------
    // POST /api/v1/articles
    // -------------------------------------------------------------------------

    @Test
    void shouldCreateArticle() throws Exception {
        ArticleResponseDto response =
                ArticleTestFactory.createArticleResponseDto(false);

        when(articleService.create(any(ArticleRequestDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "slug": "my-article",
                                    "topicSlug": "java",
                                    "title": "article title",
                                    "content": "my article content"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Jane_Doe"))
                .andExpect(jsonPath("$.title").value("article title"))
                .andExpect(jsonPath("$.slug").value("my-article"))
                .andExpect(jsonPath("$.content").value("my article content"))
                .andExpect(jsonPath("$.comments").isArray());

        ArgumentCaptor<ArticleRequestDto> captor =
                ArgumentCaptor.forClass(ArticleRequestDto.class);

        verify(articleService).create(captor.capture());

        ArticleRequestDto request = captor.getValue();

        assertEquals("my-article", request.slug());
        assertEquals("java", request.topicSlug());
        assertEquals("article title", request.title());
        assertEquals("my article content", request.content());
    }

    @Test
    void shouldReturn400WhenArticleRequestIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "slug": "",
                                    "topicSlug": "",
                                    "title": "",
                                    "content": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(articleService, never()).create(any(ArticleRequestDto.class));
    }

    @Test
    void shouldReturn400WhenTitleExceeds100Characters() throws Exception {
        String longTitle = "a".repeat(101);

        String request = """
                {
                    "slug": "my-article",
                    "topicSlug": "java",
                    "title": "%s",
                    "content": "my article content"
                }
                """.formatted(longTitle);

        mockMvc.perform(post("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verify(articleService, never()).create(any(ArticleRequestDto.class));
    }

    @Test
    void shouldReturn400WhenContentExceeds255Characters() throws Exception {
        String longContent = "a".repeat(256);

        String request = """
                {
                    "slug": "my-article",
                    "topicSlug": "java",
                    "title": "article title",
                    "content": "%s"
                }
                """.formatted(longContent);

        mockMvc.perform(post("/api/v1/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verify(articleService, never()).create(any(ArticleRequestDto.class));
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/articles/available-slug/{slug}
    // -------------------------------------------------------------------------

    @Test
    void shouldCheckSlugAvailability() throws Exception {
        when(articleService.findAvailableSlug("new-article"))
                .thenReturn(new AvailableSlugDto(true));

        mockMvc.perform(get("/api/v1/articles/available-slug/new-article"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        verify(articleService).findAvailableSlug("new-article");
    }

    @Test
    void shouldReturnUnavailableSlug() throws Exception {
        when(articleService.findAvailableSlug("existing-article"))
                .thenReturn(new AvailableSlugDto(false));

        mockMvc.perform(get(
                        "/api/v1/articles/available-slug/existing-article"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        verify(articleService).findAvailableSlug("existing-article");
    }
}