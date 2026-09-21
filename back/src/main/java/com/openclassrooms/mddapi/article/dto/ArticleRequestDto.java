package com.openclassrooms.mddapi.article.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record ArticleRequestDto(
        @NotNull
        String slug,

        @NotNull
        String topicSlug,

        @NotNull
        @Size(max = 100)
        String title,

        @NotNull
        @Size(max = 255)
        String content,

        MultipartFile media

) { }
