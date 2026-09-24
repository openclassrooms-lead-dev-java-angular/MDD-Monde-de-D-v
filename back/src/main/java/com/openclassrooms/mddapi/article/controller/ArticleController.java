package com.openclassrooms.mddapi.article.controller;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.dto.ArticleUpdateRequestDto;
import com.openclassrooms.mddapi.article.service.ArticleService;
import com.openclassrooms.mddapi.common.dto.AvailableSlugDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/articles")
@Validated
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("")
    public Page<ArticleResponseDto> getArticles(Pageable pageable) {
        return articleService
                .findAll(pageable);
    }

    @GetMapping("/{slug}")
    public ArticleResponseDto getArticleBySlug(
            @PathVariable String slug
    ) {
        return articleService.findBySlug(slug);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ArticleResponseDto createArticle(
            @Valid @RequestPart("article") ArticleRequestDto articleRequestDto,
            @RequestPart(value = "media", required = false) MultipartFile media
    ) {
        return articleService.create(articleRequestDto, media);
    }

    @PutMapping(
            name = "/{slug}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("@articleSecurity.isAuthor(#slug)")
    public ArticleResponseDto updateArticle(
            @PathVariable String slug,
            @Valid @RequestPart("article") ArticleUpdateRequestDto articleRequestDto,
            @RequestPart(value = "media", required = false) MultipartFile media
    ) {
        return articleService.update(slug, articleRequestDto, media);
    }

    @GetMapping("/available-slug/{slug}")
    public AvailableSlugDto getAvailableSlug(
            @PathVariable String slug
    ) {
        return articleService.findAvailableSlug(slug);
    }

}
