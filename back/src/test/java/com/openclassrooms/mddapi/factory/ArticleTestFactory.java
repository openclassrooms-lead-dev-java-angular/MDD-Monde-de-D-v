package com.openclassrooms.mddapi.factory;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.dto.ArticleUpdateRequestDto;
import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.entity.Topic;
import com.openclassrooms.mddapi.user.entity.User;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

public class ArticleTestFactory {

    public static Article createArticle(boolean withMedia) {

        User user = UserTestFactory.createUser();
        Topic topic = TopicTestFactory.createTopic();

        Article article = new Article()
                .setAuthor(user)
                .setTopic(topic)
                .setSlug("my-article")
                .setTitle("article title")
                .setContent("my article content");

        if (withMedia) {
            article.setMedia("/articles/1/image.jpg");
        }

        return article;
    }

    public static ArticleRequestDto createArticleRequestDto() {
        return new ArticleRequestDto(
                "my-article",
                "java",
                "Article title",
                "My article content"
        );
    }

    public static ArticleResponseDto createArticleResponseDto(boolean withMedia) {

        User user = UserTestFactory.createUser();
        TopicResponseDto topicResponseDto = TopicTestFactory.createTopicResponseDto();

        return new ArticleResponseDto(
                user.getUsername(),
                topicResponseDto,
                "my-article",
                "article title",
                "my article content",
                withMedia ? "articles/1/image.jpg" : null,
                LocalDateTimeTestFactory.generateCreatedAt(),
                LocalDateTimeTestFactory.generateUpdatedAt()
        );
    }

    public static ArticleResponseDto createUpdatedArticleResponseDto(boolean withMedia) {

        User user = UserTestFactory.createUser();
        TopicResponseDto topicResponseDto = TopicTestFactory.createTopicResponseDto();

        return new ArticleResponseDto(
                user.getUsername(),
                topicResponseDto,
                "updated-article",
                "Updated article",
                "New article content",
                withMedia ? "articles/1/image.jpg" : null,
                LocalDateTimeTestFactory.generateCreatedAt(),
                LocalDateTimeTestFactory.generateUpdatedAt()
        );
    }

    public static MockMultipartFile createArticlePart() {
        return new MockMultipartFile(
                "article",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """ 
                                { 
                                    "title": "article title", 
                                    "slug": "my-article", 
                                    "content": "my article content", 
                                    "topicSlug": "java" 
                                } 
                        """.getBytes()
        );
    }

    public static MockMultipartFile createMedia() {
        return new MockMultipartFile(
                "media",
                "image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes());
    }

    public static ArticleUpdateRequestDto createArticleUpdateRequestDto() {
        return new ArticleUpdateRequestDto(
                "updated-article",
                "Updated article",
                "New article content",
                "java",
                null,
                false);
    }

    public static ArticleUpdateRequestDto createArticleUpdateRequestDtoWithMedia() {
        return new ArticleUpdateRequestDto(
                "updated-article",
                "Updated article",
                "New article content",
                "java",
                createMedia(),
                true);
    }
}
