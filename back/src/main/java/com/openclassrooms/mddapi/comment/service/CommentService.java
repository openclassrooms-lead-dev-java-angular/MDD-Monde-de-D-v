package com.openclassrooms.mddapi.comment.service;

import com.openclassrooms.mddapi.article.entity.Article;
import com.openclassrooms.mddapi.article.service.ArticleService;
import com.openclassrooms.mddapi.comment.dto.CommentRequestDto;
import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.comment.entity.Comment;
import com.openclassrooms.mddapi.comment.mapper.CommentMapper;
import com.openclassrooms.mddapi.comment.repository.CommentRepository;
import com.openclassrooms.mddapi.user.entity.User;
import com.openclassrooms.mddapi.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for managing article comments.
 *
 * <p>Provides operations for retrieving comments associated with an article
 * and creating new comments for the currently authenticated user.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;
    private final ArticleService articleService;

    /**
     * Retrieves the comments associated with an article.
     *
     * <p>Comments are returned in descending order of creation date, with
     * pagination applied according to the provided {@link Pageable}.</p>
     *
     * @param pageable    pagination and sorting information
     * @param articleSlug the slug identifying the article
     * @return a page containing the article's comments
     */
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getArticleComments(
            Pageable pageable,
            final String articleSlug
    ) {
        Article article = articleService.loadArticleBySlug(articleSlug);

        return commentRepository
                .findAllByArticleIdOrderByCreatedAtDesc(pageable, article.getId())
                .map(commentMapper::toDto);
    }

    /**
     * Creates a new comment for an article.
     *
     * <p>The comment author is automatically assigned to the currently
     * authenticated user, and the article is identified by its slug.</p>
     *
     * @param articleSlug the slug identifying the article to comment on
     * @param commentDto  the data required to create the comment
     * @return the created comment as a response DTO
     */
    @Transactional
    public CommentResponseDto create(
            final String articleSlug,
            final CommentRequestDto commentDto
    ) {
        User author = userService.loadCurrentUserAuthor();
        Article article = articleService.loadArticleBySlug(articleSlug);

        Comment comment = commentMapper.toEntity(commentDto);
        comment.setAuthor(author);
        comment.setArticle(article);

        Comment savedComment = commentRepository.save(comment);

        log.info("comment saved with id {}", savedComment.getId());

        return commentMapper.toDto(savedComment);
    }
}
