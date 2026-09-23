package com.openclassrooms.mddapi.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticleRequestDto(
        @NotBlank
        String slug,

        @NotBlank
        String topicSlug,

        @NotBlank
        @Size(max = 100)
        String title,

        @NotBlank
        @Size(max = 255)
        String content

) { }
