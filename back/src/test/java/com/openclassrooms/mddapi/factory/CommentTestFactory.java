package com.openclassrooms.mddapi.factory;

import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.comment.dto.CommentRequestDto;
import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.comment.entity.Comment;
import com.openclassrooms.mddapi.user.entity.User;

public class CommentTestFactory {

    public static CommentRequestDto createCommentRequestDto() {
        return new CommentRequestDto(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
        );
    }

    public static Comment createComment() {
        User author = UserTestFactory.createUser();
        Article article = ArticleTestFactory.createArticle(false);

        Comment comment = new Comment();
        comment.setAuthor(author);
        comment.setArticle(article);
        comment.setContent("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
        comment.setCreatedAt(LocalDateTimeTestFactory.generateCreatedAt());

        return comment;
    }

    public static CommentResponseDto createCommentResponseDto() {
        User author = UserTestFactory.createUser();

        return new CommentResponseDto(
                author.getUsername(),
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                LocalDateTimeTestFactory.generateCreatedAt()
        );
    }
}
