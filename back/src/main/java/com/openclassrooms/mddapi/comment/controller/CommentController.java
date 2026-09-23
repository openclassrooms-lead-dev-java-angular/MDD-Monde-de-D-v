package com.openclassrooms.mddapi.comment.controller;

import com.openclassrooms.mddapi.comment.dto.CommentRequestDto;
import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing article comments.
 *
 * <p>Provides endpoints for retrieving and creating comments associated
 * with an article.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    /**
     * Retrieves the comments associated with an article.
     *
     * <p>Comments are returned in descending order of creation date
     * and support pagination and sorting through the provided
     * {@link Pageable}.</p>
     *
     * @param articleSlug the slug identifying the article
     * @param pageable    pagination and sorting information
     * @return a page containing the article's comments
     */
    @GetMapping("/article/{articleSlug}")
    public Page<CommentResponseDto> getArticleComments(
            @PathVariable String articleSlug,
            Pageable pageable
    ) {
        return commentService.getArticleComments(pageable, articleSlug);
    }

    /**
     *  Creates a new comment for an article.
     *
     *  <p>The comment is associated with the article identified by its slug.
     *  The author is determined from the currently authenticated user.</p>
     *
     *  @param articleSlug the slug identifying the article to comment on
     *  @param commentRequestDto the data required to create the comment
     *  @return the created comment
     */
    @PostMapping("/article/{articleSlug}")
    public CommentResponseDto createComment(
            @PathVariable String articleSlug,
            @Valid @RequestBody CommentRequestDto commentRequestDto
    ) {
        return commentService.create(articleSlug, commentRequestDto);
    }
}
