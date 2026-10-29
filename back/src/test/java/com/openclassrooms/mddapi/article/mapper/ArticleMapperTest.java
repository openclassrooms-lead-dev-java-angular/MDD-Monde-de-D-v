package com.openclassrooms.mddapi.article.mapper;

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
}