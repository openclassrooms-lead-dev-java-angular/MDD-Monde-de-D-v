package com.openclassrooms.mddapi.factory;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.topic.dto.TopicResponseDto;
import com.openclassrooms.mddapi.topic.entity.Topic;
import com.openclassrooms.mddapi.user.entity.User;

import java.util.ArrayList;
import java.util.List;

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
        List<CommentResponseDto> comments = new ArrayList<CommentResponseDto>();

        comments.add(new CommentResponseDto(
                "john_doe",
                "Super article !",
                LocalDateTimeTestFactory.generateCreatedAt()
        ));

        return new ArticleResponseDto(
                user.getUsername(),
                topicResponseDto,
                "my-article",
                "article title",
                "my article content",
                LocalDateTimeTestFactory.generateCreatedAt(),
                LocalDateTimeTestFactory.generateUpdatedAt(),
                comments
        );
    }
}
