package com.openclassrooms.mddapi.article.controller;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.service.ArticleService;
import com.openclassrooms.mddapi.common.dto.AvailableSlugDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing articles.
 * <p> Exposes endpoints to retrieve the article feed, fetch an article by its slug,
 * create a new article, and check slug availability. </p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/articles")
@Validated
public class ArticleController {

    private final ArticleService articleService;

    /**
     * Retrieves the authenticated user's article feed with pagination.
     *
     * @param pageable the pagination and sorting parameters
     * @return a page of articles from the user's feed
     */
    @GetMapping("/feed")
    public Page<ArticleResponseDto> getFeedArticles(Pageable pageable) {
        return articleService.findFeedArticles(pageable);
    }

    /**
     * Retrieves an article by its slug.
     *
     * @param slug the slug of the article to retrieve
     * @return the article matching the specified slug
     */
    @GetMapping("/{slug}")
    public ArticleResponseDto getArticleBySlug(
            @PathVariable String slug
    ) {
        return articleService.findBySlug(slug);
    }

    /**
     * Creates a new article using the provided data.
     *
     * @param articleRequestDto the data required to create the article
     * @return the newly created article
     */
    @PostMapping()
    public ArticleResponseDto createArticle(
            @Valid @RequestBody() ArticleRequestDto articleRequestDto
    ) {
        return articleService.create(articleRequestDto);
    }

    /**
     * Checks whether a slug is available for an article.
     *
     * @param slug the slug to check
     * @return the result indicating whether the slug is available
     */
    @GetMapping("/available-slug/{slug}")
    public AvailableSlugDto getAvailableSlug(
            @PathVariable String slug
    ) {
        return articleService.findAvailableSlug(slug);
    }
}
