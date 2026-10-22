package com.openclassrooms.mddapi.comment.controller;

import com.openclassrooms.mddapi.comment.dto.CommentRequestDto;
import com.openclassrooms.mddapi.comment.dto.CommentResponseDto;
import com.openclassrooms.mddapi.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing article comments.
 *
 * <p>Provides endpoints for retrieving and creating comments associated
 * with an article.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/articles/{slug}/comments")
public class CommentController {

    private final CommentService commentService;

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
    @PostMapping("")
    public CommentResponseDto createComment(
            @PathVariable("slug") String articleSlug,
            @Valid @RequestBody CommentRequestDto commentRequestDto
    ) {
        return commentService.create(articleSlug, commentRequestDto);
    }
}
