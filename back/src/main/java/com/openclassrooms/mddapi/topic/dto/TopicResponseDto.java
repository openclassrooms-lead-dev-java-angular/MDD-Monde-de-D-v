package com.openclassrooms.mddapi.topic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TopicResponseDto(

        @NotBlank
        String name,

        @NotBlank
        String slug,

        String description,

        Boolean subscribed,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {}
