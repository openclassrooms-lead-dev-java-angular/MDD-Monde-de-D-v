package com.openclassrooms.mddapi.article.controller;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.service.ArticleService;
import com.openclassrooms.mddapi.common.dto.AvailableSlugDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/articles")
@Validated
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/feed")
    public Page<ArticleResponseDto> getFeedArticles(Pageable pageable) {
        return articleService.findFeedArticles(pageable);
    }

    @GetMapping("/{slug}")
    public ArticleResponseDto getArticleBySlug(
            @PathVariable String slug
    ) {
        return articleService.findBySlug(slug);
    }

    @PostMapping()
    public ArticleResponseDto createArticle(
            @Valid @RequestBody() ArticleRequestDto articleRequestDto
    ) {
        return articleService.create(articleRequestDto);
    }

    @GetMapping("/available-slug/{slug}")
    public AvailableSlugDto getAvailableSlug(
            @PathVariable String slug
    ) {
        return articleService.findAvailableSlug(slug);
    }
}
