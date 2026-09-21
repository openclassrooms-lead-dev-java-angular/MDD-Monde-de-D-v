package com.openclassrooms.mddapi.article.controller;

import com.openclassrooms.mddapi.article.dto.ArticleRequestDto;
import com.openclassrooms.mddapi.article.dto.ArticleResponseDto;
import com.openclassrooms.mddapi.article.dto.AticleUpdateRequestDto;
import com.openclassrooms.mddapi.article.service.ArticleService;
import com.openclassrooms.mddapi.common.dto.AvailableSlugDto;
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

    private ArticleService articleService;

    @GetMapping("")
    public Page<ArticleResponseDto> getArticles(Pageable pageable) {
        return articleService.findAll(pageable);
    }

    @GetMapping("/{slug}")
    public ArticleResponseDto getArticleBySlug(String slug){
        return articleService.findBySlug(slug);
    }

    @PostMapping("")
    public ArticleResponseDto createArticle(
            @Valid @RequestBody ArticleRequestDto articleRequestDto

    ){
        return articleService.create(articleRequestDto);
    }

    @PutMapping("/{slug}")
    @PreAuthorize("@articleSecurity.isAuthor(#slug)")
    public ArticleResponseDto updateArticle(
            @PathVariable String slug,
            @Valid @RequestBody AticleUpdateRequestDto articleRequestDto
    ){
        return articleService.update(slug, articleRequestDto);
    }

    @GetMapping("/available-slug/{slug}")
    public AvailableSlugDto getAvailableSlug(
            @PathVariable String slug
    ) {
        return articleService.findAvailableSlug(slug);
    }

}
