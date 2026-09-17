package com.openclassrooms.mddapi.topic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TopicRequestDto(
        @NotBlank
        @Size(max = 50)
        String name,

        @NotBlank
        @Size(max = 90)
        String slug,

        @Size(max = 255)
        String description
) { }
