package com.openclassrooms.mddapi.comment.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentRequestDto(
        @NotBlank
        String content
) { }
