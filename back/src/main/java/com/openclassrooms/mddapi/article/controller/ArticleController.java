package com.openclassrooms.mddapi.article.controller;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
     * Retrieves an article by its public id.
     *
     * @param publicId the public id of the article to retrieve
     * @return the article matching the specified public id
     */
    @GetMapping("/{publicId}")
    @PreAuthorize(
            "@articleSecurity.canAccessArticle(#publicId)"
    )
    public ArticleResponseDto getArticleByPublicId(
            @PathVariable String publicId
    ) {
        return articleService.findByPublicId(publicId);
    }

    /**
     * Creates a new article using the provided data.
     * @param articleRequestDto the data required to create the article
     * @return the newly created article
     */
    @PostMapping()
    public ArticleResponseDto createArticle(
            @Valid @RequestBody() ArticleRequestDto articleRequestDto
    ) {
        return articleService.create(articleRequestDto);
    }
}
