package com.openclassrooms.mddapi.topic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TopicResponseDto(
        @NotNull
        Long id,

        @NotBlank
        String name,

        @NotBlank
        String slug,

        String description,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {}
