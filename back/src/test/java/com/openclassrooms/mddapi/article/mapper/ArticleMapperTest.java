package com.openclassrooms.mddapi.article.mapper;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.dto.ArticleUpdateRequestDto;
import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.factory.ArticleTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ArticleMapperTest {

    private static final String MEDIA_URL =
            "http://localhost:8080/uploads/";

    private ArticleMapper articleMapper;

    @BeforeEach
    void setUp() {
        articleMapper = Mappers.getMapper(ArticleMapper.class);
    }

    @Test
    void shouldMapArticleToResponseDto() {
        Article article = ArticleTestFactory.createArticle(false);

        ArticleResponseDto result =
                articleMapper.toDto(article, MEDIA_URL);

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

        assertThat(result.media())
                .isNull();
    }

    @Test
    void shouldMapArticleToResponseDtoWithMedia() {
        Article article = ArticleTestFactory.createArticle(true);

        ArticleResponseDto result =
                articleMapper.toDto(article, MEDIA_URL);

        assertThat(result).isNotNull();

        assertThat(result.media())
                .isEqualTo(MEDIA_URL + article.getMedia());
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

        assertThat(result.getMedia())
                .isNull();

        assertThat(result.getAuthor())
                .isNull();

        assertThat(result.getTopic())
                .isNull();
    }

    @Test
    void shouldUpdateArticleWithoutUpdatingMedia() {
        Article article = Article.builder()
                .slug("old-article")
                .title("Old title")
                .content("Old content")
                .media("articles/1/old-image.jpg")
                .build();

        ArticleUpdateRequestDto dto = new ArticleUpdateRequestDto(
                "updated-article",
                "java",
                "Updated title",
                "Updated content",
                false
        );

        articleMapper.updateEntity(dto, article);

        assertThat(article.getSlug())
                .isEqualTo("updated-article");

        assertThat(article.getTitle())
                .isEqualTo("Updated title");

        assertThat(article.getContent())
                .isEqualTo("Updated content");

        assertThat(article.getMedia())
                .isEqualTo("articles/1/old-image.jpg");
    }
}
