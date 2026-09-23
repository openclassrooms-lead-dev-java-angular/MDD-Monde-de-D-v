package com.openclassrooms.mddapi.article.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record ArticleUpdateRequestDto(

        String slug,

        String topicSlug,

        @Size(max = 100)
        String title,

        @Size(max = 255)
        String content,

        MultipartFile media,

        @NotNull
        Boolean updatedMedia
) { }
