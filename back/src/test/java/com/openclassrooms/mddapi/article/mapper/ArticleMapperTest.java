package com.openclassrooms.mddapi.article.mapper;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.factory.ArticleTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ArticleMapperTest {

    private ArticleMapper articleMapper;

    @BeforeEach
    void setUp() {
        articleMapper = Mappers.getMapper(ArticleMapper.class);
    }

    @Test
    void shouldMapArticleToArticleResponseDto() {
        Article article = ArticleTestFactory.createArticle(false);

        ArticleResponseDto result = articleMapper.toDto(article);

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo(article.getAuthor().getUsername());
        assertThat(result.slug()).isEqualTo(article.getSlug());
        assertThat(result.title()).isEqualTo(article.getTitle());
        assertThat(result.content()).isEqualTo(article.getContent());
    }

    @Test
    void shouldMapArticleRequestDtoToEntity() {
        ArticleRequestDto requestDto = new ArticleRequestDto(
                "my-article",
                "java",
                "Article title",
                "Article content"
        );

        Article result = articleMapper.toEntity(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getSlug()).isEqualTo(requestDto.slug());
        assertThat(result.getTitle()).isEqualTo(requestDto.title());
        assertThat(result.getContent()).isEqualTo(requestDto.content());
    }
}