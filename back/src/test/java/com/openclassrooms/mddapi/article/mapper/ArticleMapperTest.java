package com.openclassrooms.mddapi.article.mapper;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class ArticleMapperTest {

    private ArticleMapper articleMapper;

    @BeforeEach
    void setUp() {
        articleMapper = Mappers.getMapper(ArticleMapper.class);
    }

    // --------------------------------------------------
    // Article -> ArticleResponseDto
    // --------------------------------------------------

    @Test
    void toDto_shouldMapArticleToArticleResponseDto() {
        User author = new User();
        author.setUsername("testUser");

        Article article = new Article();
        article.setAuthor(author);
        article.setTitle("Test title");
        article.setContent("Test content");

        ArticleResponseDto result = articleMapper.toDto(article);

        assertNotNull(result);
        assertEquals("testUser", result.username());
        assertEquals("Test title", result.title());
        assertEquals("Test content", result.content());
    }

    @Test
    void toDto_shouldReturnNullWhenArticleIsNull() {
        ArticleResponseDto result = articleMapper.toDto(null);

        assertNull(result);
    }

    @Test
    void toDto_shouldMapNullAuthorUsername() {
        Article article = new Article();
        article.setAuthor(null);
        article.setTitle("Test title");
        article.setContent("Test content");

        ArticleResponseDto result = articleMapper.toDto(article);

        assertNotNull(result);
        assertNull(result.username());
        assertEquals("Test title", result.title());
        assertEquals("Test content", result.content());
    }

    @Test
    void toDto_shouldMapNullArticleFields() {
        Article article = new Article();

        ArticleResponseDto result = articleMapper.toDto(article);

        assertNotNull(result);
        assertNull(result.username());
        assertNull(result.title());
        assertNull(result.content());
    }

    // --------------------------------------------------
    // ArticleRequestDto -> Article
    // --------------------------------------------------

    @Test
    void toEntity_shouldMapArticleRequestDtoToArticle() {
        ArticleRequestDto articleDto = new ArticleRequestDto(
                "topicPublicId",
                "Test title",
                "Test content"
        );

        Article result = articleMapper.toEntity(articleDto);

        assertNotNull(result);
        assertEquals("Test title", result.getTitle());
        assertEquals("Test content", result.getContent());
    }

    @Test
    void toEntity_shouldReturnNullWhenArticleRequestDtoIsNull() {
        Article result = articleMapper.toEntity(null);

        assertNull(result);
    }

    @Test
    void toEntity_shouldMapNullDtoFields() {
        ArticleRequestDto articleDto = new ArticleRequestDto(
                null,
                null,
                null
        );

        Article result = articleMapper.toEntity(articleDto);

        assertNotNull(result);
        assertNull(result.getTitle());
        assertNull(result.getContent());
    }
}