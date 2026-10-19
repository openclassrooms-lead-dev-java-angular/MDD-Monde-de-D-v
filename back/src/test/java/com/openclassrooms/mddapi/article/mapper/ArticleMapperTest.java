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
    void shouldMapArticleToResponseDto() {
        Article article = ArticleTestFactory.createArticle(false);

        ArticleResponseDto result =
                articleMapper.toDto(article);

        assertThat(result).isNotNull();

        assertThat(result.username())
                .isEqualTo("Jane_Doe");

        assertThat(result.slug())
                .isEqualTo("my-article");

        assertThat(result.title())
                .isEqualTo("article title");

        assertThat(result.content())
                .isEqualTo("my article content");

        assertThat(result.topic())
                .isNotNull();

        assertThat(result.topic().slug())
                .isEqualTo("java");
    }

    @Test
    void shouldMapArticleToResponseDtoWithMedia() {
        Article article = ArticleTestFactory.createArticle(true);

        ArticleResponseDto result =
                articleMapper.toDto(article);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldMapArticleRequestDtoToEntity() {
        ArticleRequestDto dto =
                ArticleTestFactory.createArticleRequestDto();

        Article result = articleMapper.toEntity(dto);

        assertThat(result).isNotNull();

        assertThat(result.getSlug())
                .isEqualTo("my-article");

        assertThat(result.getTitle())
                .isEqualTo("Article title");

        assertThat(result.getContent())
                .isEqualTo("My article content");

        assertThat(result.getAuthor())
                .isNull();

        assertThat(result.getTopic())
                .isNull();
    }
}
